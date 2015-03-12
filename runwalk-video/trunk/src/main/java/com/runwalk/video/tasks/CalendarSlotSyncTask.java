package com.runwalk.video.tasks;

import java.awt.Dialog;
import java.awt.Window;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.model.CalendarSlotModel;
import com.runwalk.video.model.CustomerModel;
import com.runwalk.video.ui.CalendarSlotDialog;

public class CalendarSlotSyncTask<T extends CalendarSlotModel<?>> extends AbstractTask<List<T>, Void> {

	private final Callable<List<T>> calendarSlotListTask;

	private final Window parentWindow;

	private final Class<T> itemClass;

	public CalendarSlotSyncTask(Window parentWindow, Class<T> itemClass, Callable<List<T>> calendarSlotListTask) {
		super("syncToDatabase");
		this.parentWindow = parentWindow;
		this.calendarSlotListTask = calendarSlotListTask;
		this.itemClass = itemClass;
	}
	
	/**
	 * Calling this method will show an overview {@link Dialog} containing all {@link Analysis}s not yet synchronized with the application.
	 * The {@link Dialog} itself is created on the EDT and will be returned after calling {@link SwingUtilities#invokeLater(Runnable)}, which 
	 * might be useful if extra listeners need to be attached.
	 * 
	 * @param window The dialog's parent window
	 * @param signal A latch that the calling thread should wait for
	 * @param calendarSlotList A list of calendarSlots to be synchronized with the application
	 * @param clientList A list of clients to be associated with the displayed calendarSlots
	 * @return <code>false</code> if the dialog was dismissed and the result of the user actions should be discarded
	 */
	public CalendarSlotDialog<T> showCalendarSlotDialog(final Window window, final EventList<T> calendarSlotList, final EventList<CustomerModel> customerModelList) {
		CalendarSlotDialog<T> calendarSlotDialog = new CalendarSlotDialog<T>(window, calendarSlotList, customerModelList);
		calendarSlotDialog.setVisible(true);
		calendarSlotDialog.toFront();
		return calendarSlotDialog;
	}

	@Override
	protected List<T> doInBackground() throws Exception {
		message("startMessage");
		// get data to sync with calendar
		EventList<T> calendarSlotList = GlazedLists.eventList(this.calendarSlotListTask.call());
		//Collections.sort(calendarSlotList);
		message("endMessage", calendarSlotList.size());
		return calendarSlotList;
	}

	public Window getParentWindow() {
		return parentWindow;
	}	

	public Class<T> getItemClass() {
		return itemClass;
	}

}
