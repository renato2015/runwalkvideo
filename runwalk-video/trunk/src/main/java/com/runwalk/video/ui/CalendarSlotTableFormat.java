package com.runwalk.video.ui;

import org.jdesktop.application.ResourceMap;

import ca.odell.glazedlists.gui.WritableTableFormat;

import com.runwalk.video.entities.CalendarSlot;
import com.runwalk.video.entities.Client;

public class CalendarSlotTableFormat extends AbstractTableFormat<CalendarSlot<?>> implements WritableTableFormat<CalendarSlot<?>> {

	public CalendarSlotTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
		
	}

	public Object getColumnValue(CalendarSlot<?> baseObject, int column) {
		if (column == 0) {
			return baseObject.getName();
		} else if (column == 1 || column == 2) {
			return baseObject.getStartDate();
		} else if (column == 3) {
			return baseObject.getClient();
		}
		return null;
	}

	@Override
	public boolean isEditable(CalendarSlot<?> baseObject, int column) {
		return column == 3;
	}

	@Override
	public CalendarSlot<?> setColumnValue(CalendarSlot<?> baseObject,
			Object editedValue, int column) {
		if (column == 3) {
			baseObject.setClient((Client) editedValue);
		}
		return baseObject;
	}

}
