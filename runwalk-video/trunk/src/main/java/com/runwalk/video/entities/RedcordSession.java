package com.runwalk.video.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;

@SuppressWarnings("serial")
public class RedcordSession extends SerializableEntity<RedcordTableElement> implements RedcordTableElement {
	
	/**
	 * 'Synthetic' property to allow firing events when adding/removing analyses
	 */
	public static final String REDCORD_EXERCISE_COUNT = "redcordExerciseCount";
	
	@ManyToOne
	@JoinColumn(name="person_id", nullable=false )
	private Client client;
	
	@Column
	private String name;
	
	@Column(name="start_date")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date startDate;
	
	@Column(name="end_date")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date endDate;
	
	@Lob
	private String comments;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "redcordSession")
	@JoinFetch(JoinFetchType.OUTER)
	private List<RedcordExercise> redcordExercises;
	
	public RedcordSession(Client client) {
		this(client, "Sessie " + client.getRedcordSessionCount());
	}
	
	public RedcordSession(Client client, String name) {
		this.client = client;
		this.name = name;
	}

	public Client getClient() {
		return client;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public List<RedcordExercise> getRedcordExercises() {
		return redcordExercises;
	}
	
	public int getRedcordExerciseCount() {
		return getRedcordExercises().size();
	}
	
	public boolean addRedcordExercise(RedcordExercise redcordExercise) {
		int oldSize = getRedcordExerciseCount();
		boolean result = getRedcordExercises().add(redcordExercise);
		firePropertyChange(REDCORD_EXERCISE_COUNT, oldSize, getRedcordExerciseCount());
		return result;
	}
	
	public boolean removeRedcordExercise(RedcordExercise redcordExercise) {
		boolean result = false;
		if (client != null) {
			int oldSize = getRedcordExerciseCount();
			result = getRedcordExercises().remove(redcordExercise);
			firePropertyChange(REDCORD_EXERCISE_COUNT, oldSize, getRedcordExerciseCount());
		}
		return result;
	}

	public int compareTo(RedcordTableElement redcordTableElement) {
		int result = -1;
		if (redcordTableElement != null) {
			result = equals(redcordTableElement) ? 0 : getStartDate().compareTo(redcordTableElement.getStartDate());
		}
		return result;
	}

	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getName() {
		return name;
	}
	
	public boolean allowsChildren() {
		return true;
	}
	
	/**
	 * Currently defaulting to {@link ExerciseDirection#NOT_AVAILABLE}.
	 * Could be changed to show aggregate data later on.
	 * 
	 * @return the NOT_AVAILABLE enum constant
	 */
	public ExerciseDirection getExerciseDirection() {
		return ExerciseDirection.NOT_AVAILABLE;
	}

	/**
	 * Currently defaulting to {@link ExerciseType#NOT_AVAILABLE}.
	 * Could be changed to show aggregate data later on.
	 * 
	 * @return the NOT_AVAILABLE enum constant
	 */
	public ExerciseType getExerciseType() {
		return ExerciseType.NOT_AVAILABLE;
	}

}
