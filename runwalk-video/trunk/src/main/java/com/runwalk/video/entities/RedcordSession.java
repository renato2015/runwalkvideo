package com.runwalk.video.entities;

import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class RedcordSession extends SerializableEntity<RedcordTableElement> implements RedcordTableElement {

	private Date startDate;
	
	private Date endDate;
	
	private String comments;
	
	private String name;
	
	private List<RedcordExercise> redcordExercises;

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
	
	public boolean addRedcordExercise(RedcordExercise exercise) {
		return redcordExercises.add(exercise);
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
