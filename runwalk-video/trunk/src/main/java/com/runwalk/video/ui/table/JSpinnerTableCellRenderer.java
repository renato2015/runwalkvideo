package com.runwalk.video.ui.table;

import java.awt.Component;
import java.text.SimpleDateFormat;

import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.table.TableCellRenderer;

/**
 * This renderer does not work properly.. should be reviewed
 * 
 * @author Jeroen Peelaerts
 */
@SuppressWarnings("serial")
public class JSpinnerTableCellRenderer extends JSpinner implements TableCellRenderer {

	private final TableCellRenderer tableCellRenderer;
	
	/**
	 * Create a {@link TableCellRenderer} that draws a {@link JSpinner} containing
	 * a date formatted in the specified format.
	 * 
	 * @param dateFormat The dateFormat to format the content with
	 * @return The created renderer
	 */
	public static JSpinnerTableCellRenderer dateTableCellRenderer(SimpleDateFormat dateFormat) {
		DateTableCellRenderer dateTableCellRenderer = new DateTableCellRenderer(dateFormat);
		JSpinnerTableCellRenderer result = new JSpinnerTableCellRenderer(dateTableCellRenderer);
		result.setModel(new SpinnerDateModel());
		result.setEditor(new JSpinner.DateEditor(result, dateFormat.toPattern()));
		return result;
	}

	private JSpinnerTableCellRenderer(TableCellRenderer tableCellRenderer) {
		this.tableCellRenderer = tableCellRenderer;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (table.isCellEditable(row, column)) {
			getModel().setValue(value);
			return this;
		}
		return tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
	
	/**
	 * Implement this method to handle value parsing for the installed {@link JSpinner}'s model.
	 * 
	 * @param value The value to parse
	 * @return The parsed value
	 */
	public Object parseValue(Object value) {
		return value;
	}
	
	@Override
	public void repaint(long tm, int x, int y, int width, int height) { }

	@Override
	public void revalidate() { }

}