package com.runwalk.video.tasks;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.CalendarSlot;
import com.runwalk.video.model.CustomerModel;
import com.runwalk.video.ui.CalendarSlotDialog;

public class CalendarSlotSyncTask<T extends CalendarSlot<? super T>> extends AbstractTask<List<T>, Void> {

	private final EventList<CustomerModel> customerModelList;
	
	private final Callable<List<T>> calendarSlotListTask;

	private final Window parentWindow;

	private final Class<T> itemClass;

	public CalendarSlotSyncTask(Window parentWindow, Class<T> itemClass, Callable<List<T>> calendarSlotListTask, EventList<CustomerModel> customerModelList) {
		super("syncToDatabase");
		this.parentWindow = parentWindow;
		this.calendarSlotListTask = calendarSlotListTask;
		this.customerModelList = customerModelList;
		this.itemClass = itemClass;
	}
	
	/**
	 * Calling this method will show an overview {@link Dialog} containing all {@link Analysis}s not yet synchronized with the application.
	 * The {@link Dialog} itself is created on the EDT and will be returned after calling {@link SwingUtilities#invokeLater(Runnable)}, which 
	 * might be useful if extra listeners need to be attached.
	 * 
	 * @param window The dialog's parent window
	 * @param signal A latch that the calling thread should wait for
	 * @param analysisList A list of calendarSlots to be synchronized with the application
	 * @param clientList A list of clients to be associated with the displayed calendarSlots
	 * @return <code>false</code> if the dialog was dismissed and the result of the user actions should be discarded
	 * @throws InvocationTargetException Exception during capturing user actions on the EDT
	 * @throws InterruptedException Exception during capturing user actions on the EDT
	 */
	public FutureTask<CalendarSlotDialog<T>> showCalendarSlotDialog(final Window window, final CountDownLatch signal, 
			final EventList<T> analysisList, final EventList<CustomerModel> customerModelList) throws InvocationTargetException, InterruptedException {
		return new FutureTask<CalendarSlotDialog<T>>(new Callable<CalendarSlotDialog<T>>() {

			public CalendarSlotDialog<T> call() throws Exception {
				CalendarSlotDialog<T> calendarSlotDialog = new CalendarSlotDialog<T>(window, signal, analysisList, customerModelList);
				calendarSlotDialog.setVisible(true);
				calendarSlotDialog.toFront();
				return calendarSlotDialog;
			}
			
		});
	}

	@Override
	protected List<T> doInBackground() throws Exception {
		message("startMessage");
		// get data to sync with calendar
		EventList<T> calendarSlotList = GlazedLists.eventList(this.calendarSlotListTask.call());
		Collections.sort(calendarSlotList);
		List<T> result = Collections.emptyList();
		if (!calendarSlotList.isEmpty()) {
			// show the sync dialog on screen (invoke on EDT)
			CountDownLatch endSignal = new CountDownLatch(1);
			// pass the unfiltered client list to the dialog here
			FutureTask<CalendarSlotDialog<T>> edtTask = showCalendarSlotDialog(getParentWindow(), endSignal, calendarSlotList, customerModelList);
			SwingUtilities.invokeLater(edtTask);
			CalendarSlotDialog<T> calendarSlotDialog = edtTask.get();
			//CalendarSlotDialog<T> calendarSlotDialog = .get();
			// don't sync anything if the dialog was closed by the user
			WindowAdapter windowAdapter = new WindowAdapter() {
					
				@Override
				public void windowClosed(WindowEvent paramWindowEvent) {
					// TODO clear selection here.. do nothing
				}
				
			};
			calendarSlotDialog.addWindowListener(windowAdapter);
			endSignal.await();
			calendarSlotDialog.removeWindowListener(windowAdapter);
			result = calendarSlotDialog.getSelected();
		}
		message("endMessage", result.size());
		return result;
	}

	public Window getParentWindow() {
		return parentWindow;
	}	

	public Class<T> getItemClass() {
		return itemClass;
	}

}
