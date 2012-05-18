package com.runwalk.video.entities;

import java.beans.PropertyChangeListener;
import java.util.Date;

import org.jdesktop.swingx.treetable.TreeTableModel;

/**
 * This interface provides a contract that allows implementing beans to display themselves
 * in a {@link TreeTableModel}.
 *
 * @author Jeroen Peelaerts
 *
 */
public interface RedcordTableElement extends Comparable<RedcordTableElement> {
	
	public static final String EXERCISE_DIRECTION = "exerciseDirection";
	public static final String EXERCISE_TYPE = "exerciseType";
	public static final String COMMENTS = "comments";
	public static final String START_DATE = "startDate";

	Date getStartDate();

	ExerciseDirection getExerciseDirection();

	ExerciseType getExerciseType();

	String getComments();
	
	String getName();
	
    public void addPropertyChangeListener(PropertyChangeListener listener);
    
    public void removePropertyChangeListener(PropertyChangeListener listener);
	
	/**
	 * Return <code>true</code> if this table element can be a node in a tree table.
	 * 
	 * @return <code>true</code> if this is can be node
	 */
	boolean allowsChildren();

	public enum ExerciseType {
		FRONT, SIDE, BACK, SCAPULA;
	}
	
	public enum ExerciseDirection {
		LEFT, RIGHT;
	}
	
}
