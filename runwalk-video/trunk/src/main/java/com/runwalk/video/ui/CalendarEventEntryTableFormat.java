package com.runwalk.video.ui;

import java.util.Date;

import org.jdesktop.application.ResourceMap;

import com.google.common.collect.Iterables;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;

public class CalendarEventEntryTableFormat extends AbstractTableFormat<CalendarEventEntry> {

	public CalendarEventEntryTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
		
	}

	public Object getColumnValue(CalendarEventEntry baseObject, int column) {
		if (column == 0) {
			return baseObject.getTitle().getPlainText();
		} else if (column == 1 || column == 2) {
			When when = Iterables.getLast(baseObject.getTimes());
			return new Date(when.getStartTime().getValue());
		}
		return null;
	}

}
