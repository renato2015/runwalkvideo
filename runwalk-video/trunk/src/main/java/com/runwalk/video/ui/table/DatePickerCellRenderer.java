package com.runwalk.video.ui.table;

import java.awt.Component;
import java.text.DateFormat;

import javax.swing.JTable;

import org.jdesktop.swingx.table.DatePickerCellEditor;

@SuppressWarnings("serial")
public class DatePickerCellRenderer extends DateTableCellRenderer {

	private final DatePickerCellEditor editor;

	public DatePickerCellRenderer(DateFormat dateFormat) {
		super(dateFormat);
		this.editor = new DatePickerCellEditor(dateFormat) ;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (table.isCellEditable(row, column)) {
			Component tableCellEditorComponent = editor.getTableCellEditorComponent(table, value, isSelected, row, column);
			tableCellEditorComponent.setBackground(table.getSelectionBackground());
			return tableCellEditorComponent;
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

}
