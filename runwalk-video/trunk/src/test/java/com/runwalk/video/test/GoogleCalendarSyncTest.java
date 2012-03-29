package com.runwalk.video.test;

import java.awt.KeyboardFocusManager;
import java.awt.Window;

import javax.swing.JDialog;

import junit.framework.TestCase;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.runwalk.video.core.OnEdt;
import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.gdata.BaseEntryDaoService;
import com.runwalk.video.settings.AuthenticationSettings;

public class GoogleCalendarSyncTest extends TestCase {

	public void testConnectivity() {
		DaoService baseEntryDaoService = new BaseEntryDaoService(new AuthenticationSettings("user", "pwd", "url"), getClass().getSimpleName());
		Dao<CalendarEventEntry> calendarEventEntryDao = baseEntryDaoService.getDao(CalendarEventEntry.class);
		EventList<CalendarEventEntry> calendarEntries = new BasicEventList<CalendarEventEntry>();
		for (CalendarEventEntry calendarEntry : calendarEventEntryDao.getAll()) {
			if (calendarEntry.getParticipants().size() < 2) {
				// if event already has participants, it was once saved
				// check if timing is still the same, change otherwise
				for (When time : calendarEntry.getTimes()) {
					System.out.printf("Entry found:  %1s %2s\n", calendarEntry.getTitle().getPlainText(), calendarEntry.getId());
					System.out.println(time.getStartTime());
				}
				calendarEntries.add(calendarEntry);
				continue;
				// emtpy list.. not synced with db yet.. show in sync dialog..
				//entry.addParticipant(new EventWho());
			}
		}
		
	/*	if (!calendarEntries.isEmpty()) {
			JDialog syncDialog = showSyncDialog(calendarEntries);
			// let this thread wait for the dialog to notify when its ready..
			synchronized(syncDialog) {
				try {
					syncDialog.wait();
				} catch (InterruptedException e) {
					Logger.getLogger(GoogleCalendarSyncTest.class).error(e);
				}
			}
		}*/
		
	}

	@OnEdt
	private JDialog showSyncDialog(EventList<CalendarEventEntry> calendarEntries) {
		Window focusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
		// TODO pass client list here!
		/*JDialog result = new RedcordSessionDialog(focusedWindow, calendarEntries, null);
		result.setVisible(true);
		result.pack();*/
	//	return result;
		return null;
	}
	
}
