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

package com.runwalk.video.gui;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;

/** 
 * A table cell renderer for the Date class. 
 */
public class DateTableCellRenderer extends DefaultTableCellRenderer {
	
	private DateFormat format;

	public DateTableCellRenderer(DateFormat format) {
		this.format = format;
	}
	
	// TODO: not used
	public void setAsText(String s) throws java.lang.IllegalArgumentException {
		if (s.trim().length() == 0) {
			setValue(null);
			return;
		}
		/*try {
	            // TODO: setValue(format.parse(s));
	        } catch (ParseException e) {
	            String msg = NbBundle.getMessage(DateTableCellRenderer.class,
	                "IllegalDateValue", new Object[] {s}); //NOI18N
	            RuntimeException iae = new IllegalArgumentException(msg); 
	            ErrorManager.getDefault().annotate(iae, ErrorManager.USER, msg,
	                msg, e, new java.util.Date());
	            throw iae;
	        }*/
	}

	public Component getTableCellRendererComponent(javax.swing.JTable table, 
			Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
			setText(date == null ? "<geen>" : format.format(date));
		}
		return this;
	}    
}

