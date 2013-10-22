/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.runwalk.video.ui.table;

import java.awt.Component;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

/** 
 * A table cell renderer for the Date class. 
 */
@SuppressWarnings("serial")
public class DateTableCellRenderer extends DefaultTableCellRenderer {
	
	private final static String DEFAULT_VALUE = "";
	
	private final DateFormat shortDateFormat;
	
	private final DateFormat extendedDateFormat;
	
	/** Value to show when there is no date to be parsed. */
	private final String defaultValue;
	
	public DateTableCellRenderer(DateFormat shortDateFormat) {
		this(DEFAULT_VALUE, shortDateFormat);
	}
	
	public DateTableCellRenderer(String defaultValue, DateFormat shortDateFormat) {
		this(defaultValue, shortDateFormat, null);
	}
	
	public DateTableCellRenderer(DateFormat extendedDateFormat, DateFormat shortDateFormat) {
		this(DEFAULT_VALUE, extendedDateFormat, shortDateFormat);
	}

	public DateTableCellRenderer(String defaultValue, DateFormat extendedDateFormat, DateFormat shortDateFormat) {
		this.defaultValue = defaultValue;
		this.shortDateFormat = shortDateFormat;
		this.extendedDateFormat = extendedDateFormat;
	}
	
	public Component getTableCellRendererComponent(javax.swing.JTable table, 
			Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		// call super here to process all usual arguments such as isSelected and hasFocus
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value == null) {
			setText(defaultValue);
		} else {
			Date date = parseDate(value);
			DateFormat dateFormat = extendedDateFormat;
			if (shortDateFormat != null && table.isCellEditable(row, column)) {
				dateFormat = shortDateFormat;
			}
			synchronized(dateFormat) {
				setText(date == null ? defaultValue : dateFormat.format(date));
			}
		}
		return this;
	}
	
	protected Date parseDate(Object value) {
		Date result = new Date();
		if (value != null) {
			if (value instanceof Long) {
				result  = new Date((Long) value);
			} else if (!(value instanceof Date)) {
				try {
					// set the time value on the spinner, should be formatted using a dateformat..
					result = getDateFormat().parse(value.toString());
				} catch (ParseException e) {
					Logger.getLogger(getClass()).debug("Failed to parse date " + value);
				}
			} else {
				result = (Date) value;
			}
		}
		return result;
	}

	private DateFormat getDateFormat() {
		return shortDateFormat;
	}    
	
}

