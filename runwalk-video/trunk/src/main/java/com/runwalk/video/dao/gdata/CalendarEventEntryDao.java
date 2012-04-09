package com.runwalk.video.dao.gdata;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import com.google.gdata.client.ClientLoginAccountType;
import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
import com.google.gdata.client.GoogleService;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.util.ServiceException;
import com.runwalk.video.settings.AuthenticationSettings;

public class CalendarEventEntryDao extends BaseEntryDao<CalendarEventFeed, CalendarEventEntry> {
	
	private URL feedUrl;

	public CalendarEventEntryDao(AuthenticationSettings googleServiceSettings, String applicationName) {
		super(CalendarEventEntry.class);
		try {
			feedUrl = new URL(googleServiceSettings.getUrl());
			GoogleService googleService = new CalendarService(applicationName);
			googleService.setUserCredentials(googleServiceSettings.getUserName(), googleServiceSettings.getPassword(), ClientLoginAccountType.GOOGLE);
			
			UserToken userToken = (UserToken) googleService.getAuthTokenFactory().getAuthToken();
			googleService.getRequestFactory().setAuthToken(userToken);
			
			// too bad we can't retrieve the generic type parameter back from the subclassed feed
			setFeed(googleService.getFeed(feedUrl, CalendarEventFeed.class));
		} catch(Exception e) {
			// rethrow an unchecked exception
			getLogger().error("Unable to connect to calendar service.", e);
		}
	}
	
	public List<CalendarEventEntry> getFutureSlots() {
		CalendarQuery query = new CalendarQuery(feedUrl);
		query.setMinimumStartTime(DateTime.now());
		try {
			CalendarEventFeed eventFeed = getFeed().getService().query(query, CalendarEventFeed.class);
			return eventFeed.getEntries();
		} catch (IOException e) {
			getLogger().error(e);
		} catch (ServiceException e) {
			getLogger().error(e);
		}
		return Collections.emptyList();
	}

	public URL getFeedUrl() {
		return feedUrl;
	}

}
