package com.runwalk.video.ui;

import java.util.Calendar;
import java.util.Date;

import ca.odell.glazedlists.gui.WritableTableFormat;

import com.runwalk.video.entities.RedcordExercise;
import com.runwalk.video.entities.RedcordSession;
import com.runwalk.video.entities.RedcordTableElement;
import com.runwalk.video.entities.RedcordTableElement.ExerciseDirection;
import com.runwalk.video.entities.RedcordTableElement.ExerciseType;

public class RedcordTableFormat implements WritableTableFormat<RedcordTableElement> {
	
	public int getColumnCount() {
		return 6;
	}

	public String getColumnName(int column) {
		if(column == 0)      return "Sessie/Oefening";
		else if(column == 1) return "Datum";
		else if (column == 2) return "Tijdstip";
		else if(column == 3) return "Type oefening";
		else if(column == 4) return "Richting";
		else if(column == 5) return "Commentaar";
		throw new IllegalStateException();
	}

	public Object getColumnValue(final RedcordTableElement redcordTableElement, int column) {
		switch(column) {
		case 0: return redcordTableElement.getName();
		case 1: return redcordTableElement.getStartDate();
		case 2: return redcordTableElement.getStartDate();
		// perhaps return a combobox here??
		case 3: return redcordTableElement.getExerciseType();
		case 4: return redcordTableElement.getExerciseDirection();
		case 5: return redcordTableElement.getComments();
		default: return null;
		}
	}

	public boolean isEditable(RedcordTableElement redcordTableElement, int column) {
		// true for column 1 and 2 if they are not having children
		return ((column == 1 || column == 2) && redcordTableElement.allowsChildren() && 
				((RedcordSession) redcordTableElement).getRedcordExercises().isEmpty()) ||
			// true for column 3 and 4 if they have children
			((column == 3 || column == 4) && !redcordTableElement.allowsChildren());
	}
	
	public RedcordTableElement setColumnValue(RedcordTableElement redcordTableElement, Object editedValue, int column) {
		if (column == 1 && redcordTableElement.allowsChildren() && editedValue != null) {
			// add the date parsed from the datepicker to the one in the spinner
			RedcordSession redcordSession = (RedcordSession) redcordTableElement;
			Date startDate = redcordSession.getStartDate();
			startDate = startDate == null ? new Date() : startDate;
			Date newStartDate = addToDate(startDate, (Date) editedValue, Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH);
			redcordSession.setStartDate(newStartDate);
		} else if (column == 2 && redcordTableElement.allowsChildren()) {
			// add the date parsed from the spinner to the one in the datepicker
			RedcordSession redcordSession = (RedcordSession) redcordTableElement;
			Date startDate = redcordSession.getStartDate();
			startDate = startDate == null ? new Date() : startDate;
			Date newStartDate = addToDate(startDate, (Date) editedValue, Calendar.HOUR_OF_DAY, Calendar.MINUTE);
			redcordSession.setStartDate(newStartDate);
		} else if (column == 3 && !redcordTableElement.allowsChildren()) {
			((RedcordExercise) redcordTableElement).setExerciseType(parseEnumValue(ExerciseType.class, editedValue));
		} else if (column == 4 && !redcordTableElement.allowsChildren()) {
			((RedcordExercise) redcordTableElement).setExerciseDirection(parseEnumValue(ExerciseDirection.class, editedValue));
		}
		return redcordTableElement;
	}
	
	private Date addToDate(Date date1, Date date2, int... fields) {
		Calendar date1Calendar = Calendar.getInstance();
		date1Calendar.setTime(date1);
		Calendar date2Calendar = Calendar.getInstance();
		date2Calendar.setTime(date2);
		for(int field : fields) {
			date1Calendar.set(field, date2Calendar.get(field));
		}
		return date1Calendar.getTime();
	}

	
	private <T extends Enum<T>> T parseEnumValue(Class<T> enumClass, Object value) {
		T result = null;
		if (value != null) {
			if (value instanceof String) {
				String string = value.toString();
				result = Enum.valueOf(enumClass, string);
			} else if (enumClass.isAssignableFrom(value.getClass())) {
				result = enumClass.cast(value);
			}
		}
		return result;
	}
	
}
