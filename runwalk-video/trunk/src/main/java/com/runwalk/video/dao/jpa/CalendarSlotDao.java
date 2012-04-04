package com.runwalk.video.dao.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.entities.CalendarSlot;

/**
 * This {@link Dao} defines some specialized behavior for subclasses of {@link CalendarSlot}.
 * 
 * @author Jeroen Peelaerts
 */
public class CalendarSlotDao<T extends CalendarSlot<? super T>> extends JpaDao<T> {

	public CalendarSlotDao(Class<T> typeParameter, EntityManagerFactory entityManagerFactory) {
		super(typeParameter, entityManagerFactory);
	}
	
	public List<T> getFutureSlots() {
		 TypedQuery<T> query = createEntityManager().createQuery("SELECT calendarSlot FROM " + getTypeParameter().getSimpleName() + " calendarSlot WHERE calendarSlot.startDate <= :date", getTypeParameter())
				.setParameter("date", new Date());
		 return query.getResultList();
	}
	
}
