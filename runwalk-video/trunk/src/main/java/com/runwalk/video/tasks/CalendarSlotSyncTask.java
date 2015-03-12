package com.runwalk.video.tasks;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.model.CalendarSlotModel;
import com.runwalk.video.model.CustomerModel;
import com.runwalk.video.panels.AbstractTablePanel;
import com.runwalk.video.panels.CalendarSlotTablePanel;
import com.runwalk.video.panels.CustomerTablePanel;
import com.runwalk.video.ui.CalendarSlotModelTableFormat;

public class CalendarSlotSyncTask<T extends CalendarSlotModel<?>> extends AbstractTask<List<T>, Void> {

	private final Callable<List<T>> calendarSlotListTask;

	private final Class<T> itemClass;

	public CalendarSlotSyncTask(Class<T> itemClass, Callable<List<T>> calendarSlotListTask) {
		super("syncToDatabase");
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
	public JDialog showCalendarSlotDialog(final EventList<T> calendarSlotList, final AbstractTablePanel<CustomerModel> customerModelTablePanel) {
		final Window windowAncestor = SwingUtilities.getWindowAncestor(customerModelTablePanel);
		final EventList<CustomerModel> customerModelList = customerModelTablePanel.getItemList();
		final CalendarSlotTablePanel<T> calendarSlotModelTablePanel = new CalendarSlotTablePanel<T>(customerModelList);
		calendarSlotModelTablePanel.setTableFormat(new CalendarSlotModelTableFormat<T>(calendarSlotModelTablePanel.getResourceMap()));
		calendarSlotModelTablePanel.setItemList(calendarSlotList);
		JDialog calendarSlotDialog = new JDialog(windowAncestor);
		String title = calendarSlotModelTablePanel.getResourceMap().getString("calendarSlotModelTablePanel.title");
		calendarSlotDialog.setTitle(title); // NOI18N
		calendarSlotDialog.add(calendarSlotModelTablePanel);
		calendarSlotDialog.setVisible(true);
		calendarSlotDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		calendarSlotDialog.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent paramWindowEvent) {
				T selectedItem = calendarSlotModelTablePanel.getSelectedItem();
				if (selectedItem != null) {
					// find detached entity and apply modifications
					customerModelList.getReadWriteLock().writeLock().lock();
					try {
						CustomerModel customerModel = selectedItem.getCustomerModel();
						int rowIndex = customerModelList.indexOf(customerModel);
						// new customer, add it to the list
						if (rowIndex < 0) {
							customerModelList.add(customerModel);
							customerModelTablePanel.setSelectedItemRow(customerModel);
						} else {
							customerModelTablePanel.setSelectedItemRow(rowIndex);
						}
					} finally {
						customerModelList.getReadWriteLock().writeLock().unlock();
					}
					// save entity if it was modified
					if (selectedItem.isDirty()) {
						customerModelTablePanel.invokeAction(CustomerTablePanel.SAVE_ACTION, windowAncestor);
					}
				}
			}
			
		});
		calendarSlotDialog.toFront();
		calendarSlotDialog.pack();
		calendarSlotDialog.setLocationRelativeTo(null);
		calendarSlotDialog.setResizable(false);
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

	public Class<T> getItemClass() {
		return itemClass;
	}

}
