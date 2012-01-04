package com.runwalk.video.ui;

import ca.odell.glazedlists.gui.WritableTableFormat;

import com.runwalk.video.entities.RedcordExercise;
import com.runwalk.video.entities.RedcordTableElement;
import com.runwalk.video.entities.RedcordTableElement.ExerciseDirection;
import com.runwalk.video.entities.RedcordTableElement.ExerciseType;

public class RedcordTableFormat implements WritableTableFormat<RedcordTableElement> {
	
	public int getColumnCount() {
		return 5;
	}

	public String getColumnName(int column) {
		if(column == 0)      return "Sessie/Oefening";
		else if(column == 1) return "Datum aangemaakt";
		else if(column == 2) return "Type oefening";
		else if(column == 3) return "Links/Rechts";
		else if(column == 4) return "Commentaar";
		throw new IllegalStateException();
	}

	public Object getColumnValue(final RedcordTableElement redcordTableElement, int column) {
		switch(column) {
		case 0: return redcordTableElement.getName();
		case 1: return redcordTableElement.getStartDate();
		case 2: return redcordTableElement.getExerciseType();
		case 3: return redcordTableElement.getExerciseDirection();
		case 4: return redcordTableElement.getComments();
		default: return null;
		}
	}

	public boolean isEditable(RedcordTableElement redcordTableElement, int column) {
		return column == 2 && redcordTableElement.allowsChildren() ||
				column == 3 && !redcordTableElement.allowsChildren() || 
				column == 4 && ! redcordTableElement.allowsChildren();
	}

	public RedcordTableElement setColumnValue(RedcordTableElement redcordTableElement, Object editedValue, int column) {
		if (column == 2 && redcordTableElement.allowsChildren()) {
			// TODO handle date setting (using customized datepicker?)
		//	((RedcordSession) redcordTableElement).setStartDate(Date.);
		} else if (column == 3 && redcordTableElement.allowsChildren()) {
			((RedcordExercise) redcordTableElement).setExerciseDirection((ExerciseDirection) editedValue);
		} else if (column == 4 && redcordTableElement.allowsChildren()) {
			((RedcordExercise) redcordTableElement).setExerciseType((ExerciseType) editedValue);
		}
		return redcordTableElement;
	}
	
}
