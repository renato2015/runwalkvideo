package com.runwalk.video.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Instances of this class can be synced with a calendar.
 * 
 * @author Jeroen Peelaerts
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class CalendarSlot<T extends SerializableEntity<T>> extends SerializableEntity<T> {
	
	public static final String START_DATE = "startDate";
	
	@Column(name="start_date")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date startDate;
	
	@Column(name="appointment_extref")
	private String appointmentExtref;
	
	@Column(name="appointment_cancelled")
	private boolean cancelled;
	
	protected CalendarSlot() {
		startDate = new Date();
	}

	public CalendarSlot(Date startDate) {
		this.startDate = startDate; 
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public String getAppointmentExtref() {
		return appointmentExtref;
	}

	public abstract Customer getCustomer();
	
	public abstract void setCustomer(Customer customer);
	
	public abstract String getComments();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			CalendarSlot<?> other = (CalendarSlot<?>) obj;
			result = getAppointmentExtref() != null ? getAppointmentExtref().equals(other.getAppointmentExtref()) : other.getAppointmentExtref() == null;
			result &= getId() != null ? getId().equals(other.getId()) : result;
		}
		return result;
	}
	
}
