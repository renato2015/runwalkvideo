package com.runwalk.video.test.entities;

import java.beans.PropertyChangeListener;
import java.util.Date;

import org.jdesktop.application.AbstractBean;

public class Analysis extends AbstractBean implements Comparable<Analysis> {

	private Long id;
	
	private Client client;
	
	private Recording recording;
	
	private Date creationDate;

	private String comments;
	
	protected Analysis() { }
	
	public Analysis(Client client) {
		creationDate = new Date();
		this.client = client;
	}

	public Client getClient() {
		return client;
	}
	
	protected void setClient(Client client) {
		this.client = client;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	protected void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.firePropertyChange("comments", this.comments, this.comments = comments);
	}

	public Recording getRecording() {
		return recording;
	}

	/**
	 * Set a {@link Recording} and removes all of the installed {@link PropertyChangeListener}s from the previous recording if 
	 * the specified one is not the same as the one in the instance field.
	 * @param recording The recording
	 */
	public void setRecording(Recording recording) {
		this.firePropertyChange("recording", this.recording, this.recording = recording);
	}
	
	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((getCreationDate() == null) ? 0 : getCreationDate().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			Analysis other = (Analysis) obj;
			result = getCreationDate() != null ? getCreationDate().equals(other.getCreationDate()) : other.getCreationDate() == null;
//			result &= getClient() != null ? getClient().equals(other.getClient()) : result;
		}
		return result;
	}

	public int compareTo(Analysis analysis) {
		return this.equals(analysis) ? 0 : getCreationDate().compareTo(analysis.getCreationDate());
	}

	@Override
	public String toString() {
		return "Analysis [client=" + client.getFirstname() + " " + client.getName() + ", creationDate=" + creationDate	+ ", id=" + id + "]";	
	}

	public boolean hasRecording() {
		return getRecording() != null && getRecording().isRecorded();
	}

	public boolean hasCompressableRecording() {
		return getRecording() != null && getRecording().isCompressable();
	}

	public boolean hasCompressedRecording() {
		return getRecording() != null && getRecording().isCompressed();
	}

}
