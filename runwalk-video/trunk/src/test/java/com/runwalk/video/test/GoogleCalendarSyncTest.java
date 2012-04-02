package com.runwalk.video.test;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.runwalk.video.dao.CompositeDaoService;
import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.gdata.BaseEntryDaoService;
import com.runwalk.video.dao.jpa.JpaDaoService;
import com.runwalk.video.entities.RedcordSession;
import com.runwalk.video.settings.AuthenticationSettings;
import com.runwalk.video.ui.CalendarSyncService;

public class GoogleCalendarSyncTest extends TestCase {
	
	private AuthenticationSettings calendarSettings;
	private AuthenticationSettings databaseSettings;
	// dao service declaration
	
	private DaoService baseEntryDaoService, jpaDaoService;
	
	public void setUp() {
		PropertyConfigurator.configure(getClass().getClassLoader().getResource("META-INF/log4j.properties"));
		baseEntryDaoService = new BaseEntryDaoService(calendarSettings, getClass().getSimpleName());
		jpaDaoService = new JpaDaoService(databaseSettings, getClass().getSimpleName());
	}

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
	
	public void testSyncManager() {
		// create a composite daoservice ..
		DaoService daoService = new CompositeDaoService(jpaDaoService, baseEntryDaoService);
		CalendarSyncService<RedcordSession> service = new CalendarSyncService<RedcordSession>(RedcordSession.class, daoService);
		assertTrue(service.syncToCalendar());
	}
	
}
