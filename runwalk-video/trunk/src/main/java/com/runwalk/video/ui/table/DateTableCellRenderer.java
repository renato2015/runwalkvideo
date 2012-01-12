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
import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;

/** 
 * A table cell renderer for the Date class. 
 */
@SuppressWarnings("serial")
public class DateTableCellRenderer extends DefaultTableCellRenderer {
	
	private final DateFormat dateFormat;

	public DateTableCellRenderer(DateFormat format) {
		this.dateFormat = format;
	}
	
	public Component getTableCellRendererComponent(javax.swing.JTable table, 
			Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		// call super here to process all usual arguments such as isSelected and hasFocus
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value == null) {
			setText("");
		} else {
			Date date = null;
			if (value instanceof Long) {
				date  = new Date((Long) value);
			} else if (value instanceof Date) {
				date = (Date) value;
			}
			synchronized(dateFormat) {
				setText(date == null ? "<geen>" : dateFormat.format(date));
			}
		}
		return this;
	}

	public DateFormat getDateFormat() {
		return dateFormat;
	}    
	
}

