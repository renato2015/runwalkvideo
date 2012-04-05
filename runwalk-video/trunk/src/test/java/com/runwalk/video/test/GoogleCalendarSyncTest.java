package com.runwalk.video.test;

import java.util.Map;

import org.junit.Ignore;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.runwalk.video.dao.CompositeDaoService;
import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.gdata.BaseEntryDaoService;
import com.runwalk.video.dao.jpa.ClientDao;
import com.runwalk.video.dao.jpa.JpaDaoService;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.RedcordSession;
import com.runwalk.video.ui.CalendarSlotDialog;
import com.runwalk.video.ui.CalendarSyncService;

public class GoogleCalendarSyncTest extends BaseTestCase {
	
	private static final String APP_NAME = "runwalk-video";
	// dao service declaration
	private DaoService baseEntryDaoService, jpaDaoService;
	
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
				//entry.addParticipant(new EventWho());
			}
		}
	}
	
	public void testSyncManager() throws InterruptedException {
		// create a composite daoservice ..
		DaoService daoService = new CompositeDaoService(jpaDaoService, baseEntryDaoService);
		CalendarSyncService<RedcordSession> service = new CalendarSyncService<RedcordSession>(RedcordSession.class, daoService);
		
		ClientDao clientDao = daoService.getDao(Client.class);
		EventList<Client> clientList = GlazedLists.eventList(clientDao.getByIds(Sets.newHashSet(120L, 2344L)));
		
		Map<RedcordSession, CalendarEventEntry> calendarSlotMapping = service.syncToCalendar();
		EventList<RedcordSession> calendarSlotList = GlazedLists.eventList(calendarSlotMapping.keySet());
		assertNotNull(calendarSlotMapping);
		assertTrue(calendarSlotMapping.size() > 0);
		
		CalendarSlotDialog<RedcordSession> calendarSlotDialog = new CalendarSlotDialog<RedcordSession>(null, calendarSlotList, clientList);
		synchronized(calendarSlotDialog) {
			calendarSlotDialog.wait();
		}
	}
	
}
