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
import com.runwalk.video.entities.CalendarSlotStatus;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.SerializableEntity;

/**
 * A service that maps {@link CalendarSlot}s with gdata's {@link CalendarEventEntry}s.
 * 
 * @author Jeroen Peelaerts
 *
 * @param <T> The {@link SerializableEntity} subclass
 */
public class CalendarSlotSyncService<T extends CalendarSlot<? super T>> implements SyncService<T, CalendarEventEntry> {

	private final Class<T> typeParameter;
	private final DaoService daoService;
	private final Logger logger = Logger.getLogger(CalendarSlotSyncService.class);
	
	public CalendarSlotSyncService(Class<T> typeParameter, DaoService daoService) {
		this.typeParameter = typeParameter;
		this.daoService = daoService;
	}
	
	public void syncToService(Map<T, CalendarEventEntry> calendarEventEntryMapping) {
		CalendarSlotDao<T> calendarSlotDao = getDaoService().getDao(getTypeParameter());
		for (Entry<T, CalendarEventEntry> entry : calendarEventEntryMapping.entrySet()) {
			T calendarSlot = entry.getKey();
			CalendarEventEntry calendarEventEntry = entry.getValue();
			if (calendarSlot.getClient() != null) {
				// copy client information to calendarEventEntry first
				updateBaseEntry(calendarEventEntry, calendarSlot);
				if (calendarSlot.getId() == null) {
					// persist entity to database
					calendarSlotDao.persist(calendarSlot);
				} else {
					calendarSlotDao.merge(calendarSlot);
				}
			} else {
				// TODO set an ignore flag here?
				// set extended property on the calendarEventEntry
				/*ExtendedProperty property = new ExtendedProperty();
				property.setName("http://www.example.com/schemas/2005#mycal.id");
				property.setValue("true");
				calendarEventEntry.addExtendedProperty(property);*/
			}
		}
	}

	/**
	 * Prepare data for synchronization with Google Calendar. This method will get all
	 * the event entries that are scheduled at a time later than today and compare them with
	 * type <code>T</code> instances in the database.
	 * 
	 * @return A map mapping all calendarSlots to their calendarEventEntry counterparts
	 */
	public Map<T, CalendarEventEntry> prepareSyncToService() {
		Map<T, CalendarEventEntry> result = new HashMap<T, CalendarEventEntry>();
		CalendarSlotDao<T> calendarSlotDao = getDaoService().getDao(getTypeParameter());
		CalendarEventEntryDao calendarEventEntryDao = getDaoService().getDao(CalendarEventEntry.class);
		List<T> calendarSlots = calendarSlotDao.getFutureSlots();
		for (CalendarEventEntry calendarEventEntry : calendarEventEntryDao.getFutureSlots()) {
			T foundCalendarSlot = null;
			for(Iterator<T> iterator = calendarSlots.iterator(); iterator.hasNext(); ) {
				T calendarSlot = iterator.next();
				if (calendarEventEntry.getIcalUID().equals(calendarSlot.getCalendarId())) {
					foundCalendarSlot = calendarSlot;
					// remove the item from the list
					iterator.remove();
					// compare lastModified property
					if (calendarEventEntry.getUpdated().getValue() != foundCalendarSlot.getLastModified().getTime()) {
						// compare start and end dates..
						if (mapStartAndEndDate(calendarEventEntry, foundCalendarSlot)) {
							foundCalendarSlot.setCalendarSlotStatus(CalendarSlotStatus.MODIFIED);
							logger.info("Start or end date has changed for CalendarSlot " + calendarSlot.getName());
							result.put(foundCalendarSlot, calendarEventEntry);
						} else {
							// nothing to be done..
							foundCalendarSlot.setCalendarSlotStatus(CalendarSlotStatus.SYNCHRONIZED);
						}
					} 
				}
			}
			// no matching results found, create a new slot
			if (foundCalendarSlot == null) {
				foundCalendarSlot = mapBaseEntry(calendarEventEntry);
				foundCalendarSlot.setCalendarSlotStatus(CalendarSlotStatus.NEW);
				logger.info("CalendarSlot created " + foundCalendarSlot);
				result.put(foundCalendarSlot, calendarEventEntry);
			}
		}
		return result;
	}
	
	private String textToUpperCase(String arg0) {
		return arg0.length() > 0 ? Character.toUpperCase(arg0.charAt(0)) + arg0.substring(1) : arg0;
	}
	
	/**
	 * Copy the {@link CalendarSlot}'s updated information back to the {@link CalendarEventEntry}.
	 * {@link Client} information will only be copied when synchronizing for the first time.
	 * 
	 * @param calendarEventEntry The calendarEventEntry to update
	 * @param calendarSlot The calendarSlot to take the client information from
	 */
	private void updateBaseEntry(CalendarEventEntry calendarEventEntry, CalendarSlot<?> calendarSlot) {
		CalendarEventEntryDao calendarEventEntryDao = getDaoService().getDao(CalendarEventEntry.class);
		Client client = calendarSlot.getClient();
		if (calendarSlot.getCalendarSlotStatus() == CalendarSlotStatus.NEW) {
			// add the client as a participant
			EventWho eventWho = new EventWho();
			String fullClientName = textToUpperCase(client.getFirstname()) + " " + textToUpperCase(client.getName());
			eventWho.setValueString(fullClientName);
			eventWho.setEmail(client.getEmailAddress());
			calendarEventEntry.addParticipant(eventWho);
			// update the event's title
			calendarEventEntry.setTitle(new PlainTextConstruct(fullClientName + " " + calendarSlot.getName()));
			// save to calendar
			calendarEventEntryDao.merge(calendarEventEntry);
			// get the last modified field back from the entry
		}
		calendarSlot.setLastModified(new Date(calendarEventEntry.getUpdated().getValue()));
	}
	
	/**
	 * Map the start and end time of a {@link CalendarEventEntry} to the given {@link CalendarSlot}.
	 * 
	 * @param calendarEventEntry The given calendarEventEntry
	 * @param calendarSlot The calendarSlot to map the start and end times to
	 * 
	 * @return <code>true</code> if the calendarSlot needs to be updated
	 */
	private boolean mapStartAndEndDate(CalendarEventEntry calendarEventEntry, T calendarSlot) {
		boolean result = false;
		Iterator<When> iterator = calendarEventEntry.getTimes().iterator();
		if (iterator.hasNext()) {
			// set start and end dates..
			When time = iterator.next();
			if (time.getStartTime().getValue() != calendarSlot.getStartDate().getTime() ||
					time.getEndTime().getValue() != calendarSlot.getEndDate().getTime()) {
				calendarSlot.setStartDate(new Date(time.getStartTime().getValue()));
				calendarSlot.setEndDate(new Date(time.getEndTime().getValue()));
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * Map all properties from a {@link CalendarEventEntry} to a newly created {@link CalendarSlot}.
	 * 
	 * @param calendarEventEntry The given calendarEventEntry
	 * @return The mapped calendarSlot
	 */
	public T mapBaseEntry(CalendarEventEntry calendarEventEntry) {
		T result = null;
		try {
			result = getTypeParameter().newInstance();
			result.setCalendarId(calendarEventEntry.getIcalUID());
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
