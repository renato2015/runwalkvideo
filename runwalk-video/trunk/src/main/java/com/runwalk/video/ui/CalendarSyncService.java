package com.runwalk.video.ui;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gdata.data.calendar.CalendarEventEntry;
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
			for(Iterator<T> it = calendarSlots.iterator(); it.hasNext(); ) {
				T calendarSlot = it.next();
				if (calendarEventEntry.getId().equals(calendarSlot.getCalendarId())) {
					// compare start and endDate
					System.out.println("id is the same.. check start and end date here");
				} else {
					// create a new calendar slot from the event entry
					try {
						T newCalendarSlot = getTypeParameter().newInstance();
						newCalendarSlot.setCalendarId(calendarEventEntry.getId());
						// set start and end dates..
						System.out.println("new item created");
					} catch (InstantiationException e) {
						logger.error(e);
					} catch (IllegalAccessException e) {
						logger.error(e);
					}
				}
			}
		}
		return true;
	}

	private Class<T> getTypeParameter() {
		return typeParameter;
	}

	public DaoService getDaoService() {
		return daoService;
	}
	
}
