package com.runwalk.video.ui;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.EventWho;
import com.google.gdata.data.extensions.When;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.gdata.CalendarEventEntryDao;
import com.runwalk.video.dao.jpa.CalendarSlotDao;
import com.runwalk.video.entities.CalendarSlot;
import com.runwalk.video.entities.Client;

/**
 * A service that synchronizes {@link CalendarSlot} entities with an online (google) calendar.
 * 
 * @author Jeroen Peelaerts
 *
 * @param <T> The database entity class to synchronize
 */
public class CalendarSyncService<T extends CalendarSlot<? super T>> {

	private final Class<T> typeParameter;
	private final DaoService daoService;
	private final Logger logger = Logger.getLogger(CalendarSyncService.class);
	
	public CalendarSyncService(Class<T> typeParameter, DaoService daoService) {
		this.typeParameter = typeParameter;
		this.daoService = daoService;
	}
	
	public void syncToCalendar(Map<T, CalendarEventEntry> calendarEventEntryMapping) {
		CalendarSlotDao<T> calendarSlotDao = getDaoService().getDao(getTypeParameter());
		for (Entry<T, CalendarEventEntry> entry : calendarEventEntryMapping.entrySet()) {
			T calendarSlot = entry.getKey();
			CalendarEventEntry calendarEventEntry = entry.getValue();
			if (calendarSlot.getClient() != null) {
				if (calendarSlot.getId() == null) {
					// copy client information to calendarEventEntry first
					updateCalendarEventEntry(calendarEventEntry, calendarSlot);
					// persist entity to database
					calendarSlotDao.persist(calendarSlot);
				} else {
					calendarSlotDao.merge(calendarSlot);
				}
			} else {
				// TODO set an ignore flag here?
			}
		}
	}

	/**
	 * Prepare data for synchronization with Google Calendar. This method will get all
	 * the event entries that scheduled at a time later than today and compare them with
	 * instances in the database of parameter type T.
	 * 
	 * @return A map mapping all calendarSlots to their calendarEventEntry counterparts
	 */
	public Map<T, CalendarEventEntry> prepareSyncToCalendar() {
		Map<T, CalendarEventEntry> result = new HashMap<T, CalendarEventEntry>();
		CalendarSlotDao<T> calendarSlotDao = getDaoService().getDao(getTypeParameter());
		CalendarEventEntryDao calendarEventEntryDao = getDaoService().getDao(CalendarEventEntry.class);
		List<T> calendarSlots = calendarSlotDao.getFutureSlots();
		for (CalendarEventEntry calendarEventEntry : calendarEventEntryDao.getFutureSlots()) {
			T foundCalendarSlot = null;
			for(Iterator<T> iterator = calendarSlots.iterator(); iterator.hasNext(); ) {
				T calendarSlot = iterator.next();
				if (calendarEventEntry.getId().equals(calendarSlot.getCalendarId())) {
					foundCalendarSlot = calendarSlot;
					// remove the item from the list
					iterator.remove();
					// compare lastModified property
					if (calendarEventEntry.getUpdated().getValue() != foundCalendarSlot.getLastModified().getTime()) {
						mapStartAndEndDate(calendarEventEntry, foundCalendarSlot);
						logger.info("id is the same.. check start and end date here");
					}
				}
			}
			// no matching results found, create a new slot
			if (foundCalendarSlot == null) {
				foundCalendarSlot = mapFromCalendarEventEntry(calendarEventEntry);
				logger.info("new CalendarSlot created " + foundCalendarSlot);
			}
			// save the mapping between both for later use
			result.put(foundCalendarSlot, calendarEventEntry);
		}
		return result;
	}
	
	/**
	 * Copy the {@link CalendarSlot}'s {@link Client} information back to the {@link CalendarEventEntry}.
	 * 
	 * @param calendarEventEntry The calendarEventEntry to update
	 * @param calendarSlot The calendarSlot to take the client information from
	 */
	private void updateCalendarEventEntry(CalendarEventEntry calendarEventEntry, CalendarSlot<?> calendarSlot) {
		CalendarEventEntryDao calendarEventEntryDao = getDaoService().getDao(CalendarEventEntry.class);
		Client client = calendarSlot.getClient();
		if (client != null) {
			// add the client as a participant
			EventWho eventWho = new EventWho();
			String fullClientName = client.getFirstname() + " " + client.getName();
			eventWho.setValueString(fullClientName);
			eventWho.setEmail(client.getEmailAddress());
			calendarEventEntry.addParticipant(eventWho);
			// update the event's title
			calendarEventEntry.setTitle(new PlainTextConstruct(fullClientName + " " + calendarSlot.getName()));
			// save to calendar
			calendarEventEntryDao.merge(calendarEventEntry);
			// get the last modified field back from the entry
			calendarSlot.setLastModified(new Date(calendarEventEntry.getUpdated().getValue()));
		}
	}
	
	/**
	 * Map the start and end time of a {@link CalendarEventEntry} to the given {@link CalendarSlot}.
	 * 
	 * @param calendarEventEntry The given calendarEventEntry
	 * @param calendarSlot The calendarSlot to map the start and end times to
	 */
	private void mapStartAndEndDate(CalendarEventEntry calendarEventEntry, T calendarSlot) {
		Iterator<When> iterator = calendarEventEntry.getTimes().iterator();
		if (iterator.hasNext()) {
			// set start and end dates..
			When time = iterator.next();
			if (time.getStartTime().getValue() != calendarSlot.getStartDate().getTime() ||
					time.getEndTime().getValue() != calendarSlot.getEndDate().getTime()) {
				calendarSlot.setStartDate(new Date(time.getStartTime().getValue()));
				calendarSlot.setEndDate(new Date(time.getEndTime().getValue()));
			}
		}
	}
	
	/**
	 * Map all properties from a {@link CalendarEventEntry} to a newly created {@link CalendarSlot}.
	 * 
	 * @param calendarEventEntry The given calendarEventEntry
	 * @return The mapped calendarSlot
	 */
	private T mapFromCalendarEventEntry(CalendarEventEntry calendarEventEntry) {
		T result = null;
		try {
			result = getTypeParameter().newInstance();
			result.setCalendarId(calendarEventEntry.getId());
			//result.setLastModified(new Date(calendarEventEntry.getUpdated().getValue()));
			result.setName(calendarEventEntry.getTitle().getPlainText());
			mapStartAndEndDate(calendarEventEntry, result);
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		}
		return result;
	}

	private Class<T> getTypeParameter() {
		return typeParameter;
	}

	public DaoService getDaoService() {
		return daoService;
	}

}
