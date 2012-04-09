package com.runwalk.video.test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Ignore;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.google.common.collect.Sets;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.runwalk.video.core.OnEdt;
import com.runwalk.video.dao.CompositeDaoService;
import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.gdata.BaseEntryDaoService;
import com.runwalk.video.dao.jpa.ClientDao;
import com.runwalk.video.dao.jpa.JpaDaoService;
import com.runwalk.video.entities.CalendarSlot;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.RedcordSession;
import com.runwalk.video.ui.CalendarSlotDialog;
import com.runwalk.video.ui.CalendarSlotSyncService;

public class GoogleCalendarSyncTest extends BaseTestCase {
	
	private static final String APP_NAME = "runwalk-video";
	// dao service declaration
	private DaoService baseEntryDaoService, jpaDaoService;
	
	private CalendarSlotDialog<? extends CalendarSlot<?>> calendarSlotDialog;
	
	public void setUp() {
		super.setUp();
		getSettingsManager().loadSettings();
		baseEntryDaoService = new BaseEntryDaoService(getSettingsManager().getCalendarSettings(), APP_NAME);
		jpaDaoService = new JpaDaoService(getSettingsManager().getDatabaseSettings(), APP_NAME);
	}
	
	@Ignore
	public void testCalendarEventEntryDao() {
		Dao<CalendarEventEntry> calendarEventEntryDao = baseEntryDaoService.getDao(CalendarEventEntry.class);
		EventList<CalendarEventEntry> calendarEntries = new BasicEventList<CalendarEventEntry>();
		for (CalendarEventEntry calendarEntry : calendarEventEntryDao.getAll()) {
			assertNotNull(calendarEntry.getId());
			if (calendarEntry.getParticipants().size() < 2) {
				// if event already has participants, it was once saved
				// check if timing is still the same, change otherwise
				for (When time : calendarEntry.getTimes()) {
					assertNotNull(time.getStartTime());
					assertNotNull(time.getEndTime());
					System.out.printf("Entry found:  %1s %2s\n", calendarEntry.getTitle().getPlainText(), calendarEntry.getId());
					System.out.println(time.getStartTime());
				}
				calendarEntries.add(calendarEntry);
				continue;
				// emtpy list.. not synced with db yet.. show in sync dialog..
			}
		}
	}
	
	public void testSyncManager() throws InterruptedException {
		// create a composite daoservice ..
		DaoService daoService = new CompositeDaoService(jpaDaoService, baseEntryDaoService);
		CalendarSlotSyncService<RedcordSession> calendarSyncService = new CalendarSlotSyncService<RedcordSession>(RedcordSession.class, daoService);
		
		ClientDao clientDao = daoService.getDao(Client.class);
		EventList<Client> clientList = GlazedLists.eventList(clientDao.getByIds(Sets.newHashSet(120L, 2344L)));
		// get data to sync with calendar
		Map<RedcordSession, CalendarEventEntry> calendarSlotMapping = calendarSyncService.prepareSyncToService();
		EventList<RedcordSession> calendarSlotList = GlazedLists.eventList(calendarSlotMapping.keySet());
		// sort appointments according to date
		Collections.sort(calendarSlotList);
		assertNotNull(calendarSlotMapping);
		// show the sync dialog on screen (invoke on EDT)
		CountDownLatch endSignal = new CountDownLatch(1);
		showCalendarSlotDialog(endSignal, calendarSlotList, clientList);
		endSignal.await();
		calendarSlotDialog.setVisible(false);
		// continue to work after notification from the dialog
		calendarSyncService.syncToService(calendarSlotMapping);
	}

	@OnEdt
	private void showCalendarSlotDialog(final CountDownLatch endSignal, final EventList<RedcordSession> calendarSlotList, final EventList<Client> clientList) {
		calendarSlotDialog = new CalendarSlotDialog<RedcordSession>(null, endSignal, calendarSlotList, clientList);
		calendarSlotDialog.setVisible(true);
		calendarSlotDialog.toFront();
	}
	
}
