package com.runwalk.video.entities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;

@SuppressWarnings("serial")
@Entity
@Table(name="redcord_sessions")
public class RedcordSession extends CalendarSlot<RedcordTableElement> implements RedcordTableElement {
	
	@Lob
	private String comments;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "redcordSession")
	@JoinFetch(JoinFetchType.OUTER)
	private List<RedcordExercise> redcordExercises = new ArrayList<RedcordExercise>();
	
	public RedcordSession() {	}
	
	public RedcordSession(Client client, String name) {
		// set default starting date to 8 o'clock current day
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		setStartDate(calendar.getTime());
		setClient(client);
		setName(name);
	}

	public List<RedcordExercise> getRedcordExercises() {
		return redcordExercises;
	}
	
	public int getRedcordExerciseCount() {
		return getRedcordExercises().size();
	}
	
	public boolean addRedcordExercise(RedcordExercise redcordExercise) {
		boolean result = getRedcordExercises().add(redcordExercise);
		getClient().incrementRedcordTableElementCount();
		return result;
	}
	
	public boolean removeRedcordExercise(RedcordTableElement redcordExercise) {
		boolean result = false;
		if (getClient() != null) {
			result = getRedcordExercises().remove(redcordExercise);
			getClient().decrementRedcordTableElementCount();
		}
		return result;
	}

	public int compareTo(RedcordTableElement redcordTableElement) {
		int result = -1;
		if (redcordTableElement != null) {
			if (equals(redcordTableElement)) {
				result = 0;
			} else if (getStartDate() != null && redcordTableElement.getStartDate() != null) {
				result = getStartDate().compareTo(redcordTableElement.getStartDate());
			}
		}
		return result;
	}
	
	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		firePropertyChange(COMMENTS, this.comments, this.comments = comments);
	}

	public boolean allowsChildren() {
		return true;
	}
	
	public boolean isSynthetic() {
		return getRedcordExerciseCount() > 0;
	}
	
	/**
	 * Currently returning <code>null</code>.
	 * Could be changed to show aggregate data later on.
	 * 
	 * @return the NOT_AVAILABLE enum constant
	 */
	public ExerciseDirection getExerciseDirection() {
		return null;
	}

	/**
	 * Currently returning <code>null</code>.
	 * Could be changed to show aggregate data later on.
	 * 
	 * @return the NOT_AVAILABLE enum constant
	 */
	public ExerciseType getExerciseType() {
		return null;
	}

	@Override
	public String toString() {
		return "RedcordSession [client=" + getClient() + ", name=" + getName()
				+ ", startDate=" + getStartDate() + "]";
	}

}
