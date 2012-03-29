package com.runwalk.video.ui;

import org.jdesktop.application.ResourceMap;

import com.runwalk.video.entities.RedcordSession;

public class RedcordSessionTableFormat extends AbstractTableFormat<RedcordSession> {

	public RedcordSessionTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
		
	}

	public Object getColumnValue(RedcordSession baseObject, int column) {
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
