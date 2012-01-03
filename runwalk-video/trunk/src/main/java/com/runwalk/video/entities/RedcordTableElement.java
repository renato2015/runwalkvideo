package com.runwalk.video.entities;

import java.util.Date;

/**
 * This interface provides a contract that allows implementing beans to display themselves
 * in a tree table.
 *
 * @author Jeroen Peelaerts
 *
 */
public interface RedcordTableElement extends Comparable<RedcordTableElement> {
	
	Date getStartDate();

	ExerciseDirection getExerciseDirection();

	ExerciseType getExerciseType();

	String getComments();
	
	String getName();
	
	/**
	 * Return <code>true</code> if this table element can be a node in a tree table.
	 * 
	 * @return <code>true</code> if this is can be node
	 */
	boolean allowsChildren();

	public enum ExerciseType {
		FRONT, SIDE, BACK, SCAPULA, NOT_AVAILABLE;
	}
	
	public enum ExerciseDirection {
		LEFT, RIGHT, NOT_AVAILABLE;
	}
	
}
