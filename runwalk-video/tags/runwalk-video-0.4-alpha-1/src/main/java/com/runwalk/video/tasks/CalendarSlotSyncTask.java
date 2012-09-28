package com.runwalk.video.tasks;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.CalendarSlot;
import com.runwalk.video.entities.Client;
import com.runwalk.video.ui.CalendarSlotSyncService;

public class CalendarSlotSyncTask<T extends CalendarSlot<? super T>> extends AbstractTask<List<T>, Void> {

	private final EventList<Client> clientList;
	
	private final Window parentWindow;

	private final Class<T> itemClass;

	private final DaoService daoService;
	
	public CalendarSlotSyncTask(Window parentWindow, DaoService daoService, Class<T> itemClass, EventList<Client> clientList) {
		super("syncToDatabase");
		this.parentWindow = parentWindow;
		this.clientList = clientList;
		this.itemClass = itemClass;
		this.daoService = daoService;
	}

	@Override
	protected List<T> doInBackground() throws Exception {
		message("startMessage");
		CalendarSlotSyncService<T> calendarSyncService = new CalendarSlotSyncService<T>(getDaoService(), getItemClass());
		// get data to sync with calendar
		final Map<T, CalendarEventEntry> calendarSlotMapping = calendarSyncService.prepareSyncToDatabase();
		EventList<T> calendarSlotList = GlazedLists.eventList(calendarSlotMapping.keySet());
		// sort appointments according to date
		Collections.sort(calendarSlotList);
		if (!calendarSlotList.isEmpty()) {
			// show the sync dialog on screen (invoke on EDT)
			CountDownLatch endSignal = new CountDownLatch(1);
			// pass the unfiltered client list to the dialog here
			Window calendarSlotDialog = calendarSyncService.showCalendarSlotDialog(getParentWindow(), endSignal, calendarSlotList, getClientList()).get();
			// don't sync anything if the dialog was closed by the user
			WindowAdapter windowAdapter = new WindowAdapter() {

				@Override
				public void windowClosed(WindowEvent paramWindowEvent) {
					calendarSlotMapping.clear();
				}
				
			};
			calendarSlotDialog.addWindowListener(windowAdapter);
			endSignal.await();
			calendarSlotDialog.removeWindowListener(windowAdapter);
		}
		// continue to work after notification from the dialog
		List<T> redcordSessions = calendarSyncService.syncToDatabase(calendarSlotMapping);
		message("endMessage", redcordSessions.size());
		return redcordSessions;
	}

	public EventList<Client> getClientList() {
		return clientList;
	}

	public Window getParentWindow() {
		return parentWindow;
	}	

	public Class<T> getItemClass() {
		return itemClass;
	}

	public DaoService getDaoService() {
		return daoService;
	}

}
