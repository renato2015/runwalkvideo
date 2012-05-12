package com.runwalk.video.ui;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.EventWho;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.extensions.When;
import com.runwalk.video.core.OnEdt;
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

	/** {@link ExtendedProperty} used to store ignore flag */
	public static final String EXTENDED_IGNORE_PROPERTY = "http://schemas.google.com/g/2005#calendarSlot.ignore";
	
	private final Class<T> typeParameter;
	private final DaoService daoService;
	private final Logger logger = Logger.getLogger(CalendarSlotSyncService.class);

	public CalendarSlotSyncService(Class<T> typeParameter, DaoService daoService) {
		this.typeParameter = typeParameter;
		this.daoService = daoService;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<T> syncToDatabase(Map<T, CalendarEventEntry> calendarEventEntryMapping) {
		List<T> result = new ArrayList<T>();
		CalendarSlotDao<T> calendarSlotDao = getDaoService().getDao(getTypeParameter());
		for (Entry<T, CalendarEventEntry> entry : calendarEventEntryMapping.entrySet()) {
			T calendarSlot = entry.getKey();
			CalendarEventEntry calendarEventEntry = entry.getValue();
			updateBaseEntry(calendarEventEntry, calendarSlot);
			// check if the calendarSlot needs to be updated
			if (!calendarSlot.isIgnored() && calendarSlot.getClient() != null) {
				if (calendarSlot.getCalendarSlotStatus().needsUpdate()) {
					// get the last modified field back from the updated entry
					result.add(calendarSlot);
					mapLastModifiedDate(calendarEventEntry, calendarSlot);
					if (calendarSlot.getId() == null) {
						// persist entity to database
						calendarSlotDao.persist(calendarSlot);
					} else {
						calendarSlotDao.merge(calendarSlot);
					}
					calendarSlot.setCalendarSlotStatus(CalendarSlotStatus.SYNCHRONIZED);
				} else if (calendarSlot.getCalendarSlotStatus().isRemoved()) {
					result.add(calendarSlot);
					calendarSlotDao.delete(calendarSlot);
				}
			}
		}
		return result;
	}

	/**
	 * Prepare data for synchronization with Google Calendar. This method will get all
	 * the event entries that are scheduled at a time later than today from the calendar 
	 * and synchronize their corresponding entities in the database.
	 * 
	 * @return A map mapping all calendarSlots to their calendarEventEntry counterparts
	 */
	public Map<T, CalendarEventEntry> prepareSyncToDatabase() {
		Map<T, CalendarEventEntry> result = new HashMap<T, CalendarEventEntry>();
		CalendarSlotDao<T> calendarSlotDao = getDaoService().getDao(getTypeParameter());
		CalendarEventEntryDao calendarEventEntryDao = getDaoService().getDao(CalendarEventEntry.class);
		List<T> calendarSlots = calendarSlotDao.getFutureSlots();
		List<CalendarEventEntry> calendarEventEntries = calendarEventEntryDao.getFutureSlots();
		for(CalendarEventEntry calendarEventEntry : calendarEventEntries) {
			if (!isIgnored(calendarEventEntry)) {
				T foundCalendarSlot = null;
				for(Iterator<T> calendarSlotIt = calendarSlots.iterator(); calendarSlotIt.hasNext(); ) {
					T calendarSlot = calendarSlotIt.next();
					if (calendarEventEntry.getIcalUID().equals(calendarSlot.getCalendarId())) {
						foundCalendarSlot = calendarSlot;
						// remove the item from the list
						calendarSlotIt.remove();
						prepareModifiedSlot(result, calendarSlot, calendarEventEntry);
					}
				}
				// no results found, create a new calendarSlot
				prepareNewSlot(result, foundCalendarSlot, calendarEventEntry);
			}
		}
		// check for removed items
		prepareRemovedSlots(result, calendarSlots);
		return result;
	}
	
	private void prepareNewSlot(Map<T, CalendarEventEntry> calendarSlotMapping, T calendarSlot, 
			CalendarEventEntry calendarEventEntry) {
		if (calendarSlot == null) {
			calendarSlot = mapBaseEntry(calendarEventEntry);
			logger.info("CalendarSlot created " + calendarSlot);
			calendarSlotMapping.put(calendarSlot, calendarEventEntry);
		}
	}
	
	private void prepareModifiedSlot(Map<T, CalendarEventEntry> calendarSlotMapping, T calendarSlot, 
			CalendarEventEntry calendarEventEntry) {
		// compare lastModified property
		if (calendarEventEntry.getUpdated().getValue() != calendarSlot.getLastModified().getTime()) {
			// compare start and end dates..
			calendarSlot.setCalendarSlotStatus(CalendarSlotStatus.MODIFIED);
			if (mapStartAndEndDate(calendarEventEntry, calendarSlot)) {
				logger.info("Start or end date has changed for CalendarSlot " + calendarSlot.getName());
				calendarSlotMapping.put(calendarSlot, calendarEventEntry);
			} else {
				// something changed.. but is of no interest
				logger.info("Last modified date has changed for CalendarSlot " + calendarSlot.getName());
				mapLastModifiedDate(calendarEventEntry, calendarSlot);
				// just update the last modified date
				CalendarSlotDao<T> calendarSlotDao = getDaoService().getDao(getTypeParameter());
				calendarSlotDao.merge(calendarSlot);
			}
		} else {
			// everything up to date!
			calendarSlot.setCalendarSlotStatus(CalendarSlotStatus.SYNCHRONIZED);
		}
	}
	
	private void prepareRemovedSlots(Map<T, ?> calendarSlotMapping, List<T> calendarSlots) {
		if (!calendarSlots.isEmpty()) {
			for(T removedCalendarSlot : calendarSlots) {
				if (removedCalendarSlot.getCalendarId() != null) {
					calendarSlotMapping.put(removedCalendarSlot, null);
					removedCalendarSlot.setCalendarSlotStatus(CalendarSlotStatus.REMOVED);
				}
			}
		}
	}

	/**
	 * Check if the ignore flag is set on the given {@link CalendarEventEntry}.
	 * 
	 * @param calendarEventEntry The given calendarEventEntry
	 * @return <code>true</code> if the ignore flag is set
	 */
	private boolean isIgnored(CalendarEventEntry calendarEventEntry) {
		for(ExtendedProperty extendedProperty : calendarEventEntry.getExtendedProperty()) {
			if (EXTENDED_IGNORE_PROPERTY.equals(extendedProperty.getName())) {
				return true;
			}
		}
		return false;
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
	 * 
	 * @return <code>true</code> if the baseEntry was updated
	 */
	private boolean updateBaseEntry(CalendarEventEntry calendarEventEntry, CalendarSlot<?> calendarSlot) {
		boolean result = false;
		CalendarEventEntryDao calendarEventEntryDao = getDaoService().getDao(CalendarEventEntry.class);
		// check if we need to update the event entry
		Client client = calendarSlot.getClient();
		if (!calendarSlot.isIgnored() && client != null && 
				calendarSlot.getCalendarSlotStatus() == CalendarSlotStatus.NEW) {
			// add the client as a participant
			EventWho eventWho = new EventWho();
			String fullClientName = textToUpperCase(client.getFirstname()) + " " + textToUpperCase(client.getName());
			eventWho.setValueString(fullClientName);
			eventWho.setEmail(client.getEmailAddress());
			calendarEventEntry.addParticipant(eventWho);
			// update the event's title
			// TODO it would be nice to have getName() returning the session number
			calendarEventEntry.setTitle(new PlainTextConstruct(fullClientName));
			// update base entry
			calendarEventEntryDao.merge(calendarEventEntry);
		} else if (calendarSlot.isIgnored()) {
			// set ignore flag on the calendarEventEntry
			ExtendedProperty property = new ExtendedProperty();
			property.setName(EXTENDED_IGNORE_PROPERTY);
			property.setValue(Boolean.toString(calendarSlot.isIgnored()));
			calendarEventEntry.addExtendedProperty(property);
			// update base entry
			calendarEventEntryDao.merge(calendarEventEntry);
		}
		return result;
	}
	
	/**
	 * Map the last modified time of a {@link CalendarEventEntry} to the given {@link CalendarSlot}.
	 * 
	 * @param calendarEventEntry The given calendarEventEntry
	 * @param calendarSlot The calendarSlot to map the start and end times to
	 */
	private void mapLastModifiedDate(CalendarEventEntry calendarEventEntry, CalendarSlot<?> calendarSlot) {
		if (calendarSlot != null && calendarEventEntry != null) {
			calendarSlot.setLastModified(new Date(calendarEventEntry.getUpdated().getValue()));
		}
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
			result.setName(calendarEventEntry.getTitle().getPlainText());
			result.setCalendarSlotStatus(CalendarSlotStatus.NEW);
			mapStartAndEndDate(calendarEventEntry, result);
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		}
		return result;
	}
	
	@OnEdt
	public void showCalendarSlotDialog(Window parentWindow, final CountDownLatch endSignal, final EventList<T> calendarSlotList, final EventList<Client> clientList) {
		CalendarSlotDialog<T> calendarSlotDialog = new CalendarSlotDialog<T>(parentWindow, endSignal, calendarSlotList, clientList);
		calendarSlotDialog.setVisible(true);
		calendarSlotDialog.toFront();
	}

	private Class<T> getTypeParameter() {
		return typeParameter;
	}

	public DaoService getDaoService() {
		return daoService;
	}

}
