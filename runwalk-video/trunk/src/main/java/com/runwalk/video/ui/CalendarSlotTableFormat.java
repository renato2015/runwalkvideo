package com.runwalk.video.ui;

import org.jdesktop.application.ResourceMap;

import com.runwalk.video.entities.CalendarSlot;

public class CalendarSlotTableFormat extends AbstractTableFormat<CalendarSlot<?>> {

	public CalendarSlotTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
		
	}

	public Object getColumnValue(CalendarSlot<?> baseObject, int column) {
		if (column == 0) {
			return baseObject.getName();
		} else if (column == 1 || column == 2) {
			return baseObject.getStartDate();
		} else if (column == 3) {
			return baseObject.getEndDate();
		} else if (column == 4) {
			return baseObject.getClient();
		}
		return null;
	}

}
