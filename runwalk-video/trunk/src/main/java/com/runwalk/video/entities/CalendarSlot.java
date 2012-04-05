package com.runwalk.video.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Instances of this class can be syned with a calendar.
 * 
 * @author Jeroen Peelaerts
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class CalendarSlot<T> extends SerializableEntity<T> {
	
	public static final String START_DATE = "startDate";

	public static final String END_DATE = "endDate";
	
	@Column(name="last_modified")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date lastModified;
	
	@Column(name="start_date")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date startDate;
	
	@Column(name="end_date")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date endDate;
	
	@Column(name="cal_id")
	private String calendarId;
	
	@ManyToOne
	@JoinColumn(name="person_id", nullable=false )
	private Client client;

	@Column
	protected String name;
	
	protected CalendarSlot() {
		this("");
	}
	
	protected CalendarSlot(String name) {
		Date date = new Date();
		startDate = date;
		lastModified = date;
		this.name = name;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		firePropertyChange(START_DATE, this.startDate, this.startDate = startDate);
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		firePropertyChange(END_DATE, this.endDate, this.endDate = endDate);
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Date getLastModified() {
		return lastModified;
	}
	
	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
