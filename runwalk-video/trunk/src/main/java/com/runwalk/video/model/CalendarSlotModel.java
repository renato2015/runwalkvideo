package com.runwalk.video.model;

import java.util.Date;

import com.runwalk.video.entities.SerializableEntity;

/**
 * Instances of this class can be synced with a calendar.
 * 
 * @author Jeroen Peelaerts
 */
public abstract class CalendarSlotModel<T extends SerializableEntity<? super T>> extends AbstractEntityModel<T> {

	public static final String START_DATE = "startDate";
	
	public CalendarSlotModel(T entity) {
		super(entity);
	}
	
	public abstract Date getStartDate();

	public abstract CustomerModel getCustomerModel();
	
	public abstract void setCustomerModel(CustomerModel customerModel);
	
	public abstract String getComments();
	
	public abstract Date getCreationDate();
	
	public abstract boolean isCancelled();
	
	public enum CalendarSlotStatus {

		NEW("calendarSlotStatus.new"),
		
		MODIFIED("calendarSlotStatus.modified"),
		
		SYNCHRONIZED("calendarSlotStatus.synchronized"),
		
		CANCELLED("calendarSlotStatus.cancelled");
		
		private final String resourceKey;
		
		private CalendarSlotStatus(String resourceKey) {
			this.resourceKey = resourceKey;
		}

		public String getResourceKey() {
			return resourceKey;
		}
		
	}

}
