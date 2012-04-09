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
	
	private final DateFormat dateFormat;
	
	/** Value to show when there is no date to be parsed. */
	private final String defaultValue;
	
	public DateTableCellRenderer(DateFormat format) {
		this("", format);
	}

	public DateTableCellRenderer(String emptyDate, DateFormat format) {
		this.dateFormat = format;
		this.defaultValue = emptyDate;
	}
	
	public Component getTableCellRendererComponent(javax.swing.JTable table, 
			Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		// call super here to process all usual arguments such as isSelected and hasFocus
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value == null) {
			setText("");
		} else {
			Date date = parseDate(value);
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
		return dateFormat;
	}    
	
}

