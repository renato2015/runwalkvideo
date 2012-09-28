package com.runwalk.video.ui;

import java.util.ArrayList;
import java.util.List;

import org.jdesktop.application.ResourceMap;

import ca.odell.glazedlists.gui.TableFormat;

public abstract class AbstractTableFormat<T> implements TableFormat<T> {

	private final List<String> columnNames = new ArrayList<String>();
	
	private final ResourceMap resourceMap;

	public AbstractTableFormat(ResourceMap resourceMap) {
		this.resourceMap = resourceMap;
		String columnTitle = null;
		for (int i = 1; i == 1 || columnTitle != null; i++) {
			columnTitle = getResourceString("columnModel.col" + i);
			if (columnTitle != null) {
				columnNames.add(columnTitle);
			}
		}
	}

	public int getColumnCount() {
		return columnNames.size();
	}

	public String getColumnName(int column) {
		if (columnNames.size() <= column) {
			throw new IllegalStateException("Title for column " + column +  " is not defined in properties file");
		}
		return columnNames.get(column);
	}

	protected String getResourceString(String key, Object... args) {
		String simpleClassName = getClass().getSimpleName();
		String resourcePrefix = Character.toLowerCase(simpleClassName.charAt(0)) + simpleClassName.substring(1);
		return resourceMap.getString(resourcePrefix + "." + key, args);
	}
	
}