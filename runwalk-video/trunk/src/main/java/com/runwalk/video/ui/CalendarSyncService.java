package com.runwalk.video.ui;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.gdata.CalendarEventEntryDao;
import com.runwalk.video.dao.jpa.CalendarSlotDao;
import com.runwalk.video.entities.CalendarSlot;

/**
 * A service that syncs {@link CalendarSlot} entities with an online (google) calendar
 * 
 * @author Jeroen Peelaerts
 */
public class CalendarSyncService<T extends CalendarSlot<? super T>> {

	private final Class<T> typeParameter;
	private final DaoService daoService;
	private final Logger logger = Logger.getLogger(CalendarSyncService.class);

	public CalendarSyncService(Class<T> typeParameter, DaoService daoService) {
		this.typeParameter = typeParameter;
		this.daoService = daoService;
	}

	public boolean syncToCalendar() {
		CalendarSlotDao<T> calendarSlotDao = getDaoService().getDao(getTypeParameter());
		CalendarEventEntryDao calendarEventEntryDao = getDaoService().getDao(CalendarEventEntry.class);
		List<T> calendarSlots = calendarSlotDao.getFutureSlots();
		for (CalendarEventEntry calendarEventEntry : calendarEventEntryDao.getFutureSlots()) {
			T correspondingSlot = null;
			for(Iterator<T> iterator = calendarSlots.iterator(); iterator.hasNext(); ) {
				T calendarSlot = iterator.next();
				if (calendarEventEntry.getId().equals(calendarSlot.getCalendarId())) {
					correspondingSlot = calendarSlot;
					// remove the item from the list
					iterator.remove();
					// compare start and endDate
					logger.info("id is the same.. check start and end date here");
				}
			}
			// no matching results found, create a new slot
			if (correspondingSlot == null) {
				T calendarSlot = mapFromCalendarEventEntry(calendarEventEntry);
				logger.info("new CalendarSlot created " + calendarSlot);
			}
		}
		return true;
	}
	
	private T mapFromCalendarEventEntry(CalendarEventEntry calendarEventEntry) {
		T result = null;
		try {
			result = getTypeParameter().newInstance();
			result.setCalendarId(calendarEventEntry.getId());
			result.setLastModified(new Date(calendarEventEntry.getUpdated().getValue()));
			Iterator<When> iterator = calendarEventEntry.getTimes().iterator();
			if (iterator.hasNext()) {
				// set start and end dates..
				When time = iterator.next();
				result.setStartDate(new Date(time.getStartTime().getValue()));
				result.setEndDate(new Date(time.getEndTime().getValue()));
			}
			
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
