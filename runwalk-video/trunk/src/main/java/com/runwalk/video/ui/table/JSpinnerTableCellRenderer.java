package com.runwalk.video.ui.table;

import java.awt.Component;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JSpinner;
import javax.swing.JTable;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class JSpinnerTableCellRenderer extends DateTableCellRenderer {

	private final JSpinner spinner;

	public JSpinnerTableCellRenderer(JSpinner spinner, DateFormat dateFormat) {
		super(dateFormat);
		this.spinner = spinner;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (table.isCellEditable(row, column)) {
			spinner.getModel().setValue(parseDate(value));
			return spinner;
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

	private Date parseDate(Object value) {
		Date result = new Date();
		if (value != null) {
			if (!(value instanceof Date)) {
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

}