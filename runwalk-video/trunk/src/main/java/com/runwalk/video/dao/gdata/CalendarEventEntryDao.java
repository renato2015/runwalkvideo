package com.runwalk.video.dao.gdata;

import java.net.URL;

import org.apache.log4j.Logger;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.runwalk.video.settings.AuthenticationSettings;

public class CalendarEventEntryDao extends BaseEntryDao<CalendarEventFeed, CalendarEventEntry> {

	public CalendarEventEntryDao(AuthenticationSettings googleServiceSettings, String applicationName) {
		super(CalendarEventEntry.class);
		try {
			GoogleService googleService = new CalendarService(applicationName);
			googleService.setUserCredentials(googleServiceSettings.getUserName(), googleServiceSettings.getPassword());
			// too bad we can't retrieve the generic type parameter back from the subclassed feed
			setFeed(googleService.getFeed(new URL(googleServiceSettings.getUrl()), CalendarEventFeed.class));
		} catch(Exception e) {
			// rethrow an unchecked exception
			Logger.getLogger(CalendarEventEntryDao.class).error("Unable to connect to calendar service.", e);
		}
	}

}
