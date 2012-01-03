package com.runwalk.video.ui;

import ca.odell.glazedlists.gui.WritableTableFormat;

import com.runwalk.video.entities.RedcordTableElement;

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

	public boolean isEditable(RedcordTableElement baseObject, int column) {
		return column == 1;
	}

	public RedcordTableElement setColumnValue(RedcordTableElement redcordTableElement, Object editedValue, int column) {
		/*if (editedValue instanceof ExerciseType) {
			redcordTableElement.setArticle((Article) editedValue);
		}*/
		// TODO handle set values
		return redcordTableElement;
	}
	
}
