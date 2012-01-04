package com.runwalk.video.entities;

import java.util.Date;

@SuppressWarnings("serial")
/**
 * Bean that represents information regarding an exercise performed during a Redcord workout.
 * 
 * @author Jeroen Peelaerts
 */
public class RedcordExercise extends SerializableEntity<RedcordTableElement> implements RedcordTableElement {
	
	private ExerciseType exerciseType;
	
	private ExerciseDirection exerciseDirection;
	
	private String comments;
	
	private String name;
	
	public ExerciseType getExerciseType() {
		return exerciseType;	
	}
	
	public void setExerciseDirection(ExerciseDirection exerciseDirection) {
		this.exerciseDirection = exerciseDirection;
	}

	public ExerciseDirection getExerciseDirection() {
		return exerciseDirection;
	}
	
	public void setExerciseType(ExerciseType exerciseType) {
		this.exerciseType = exerciseType;
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

	public Date getStartDate() {
		return null;
	}
	
	public boolean allowsChildren() {
		return false;
	}
	
	public int compareTo(RedcordTableElement redcordTableElement) {
		int result = 1;
		if (redcordTableElement != null) {
			result = equals(redcordTableElement) ? 0 : getExerciseType().compareTo(redcordTableElement.getExerciseType());
		}
		return result;
	}

}
