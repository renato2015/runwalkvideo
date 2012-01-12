package com.runwalk.video.ui.table;

import java.awt.Component;
import java.text.ParseException;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class JSpinnerTableCellEditor extends AbstractCellEditor implements TableCellEditor {
	
	private final JSpinner spinner;

	public JSpinnerTableCellEditor(JSpinner spinner) {
		this.spinner = spinner;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
			int row, int column) {
		spinner.setBackground(table.getBackground());
		if (value != null) {
			spinner.setValue(value);
		}
		return spinner;
	}

	public Object getCellEditorValue() {
		return spinner.getValue();
	}

	@Override
	public void cancelCellEditing() {
		// TODO Auto-generated method stub
		super.cancelCellEditing();
	}

	@Override
	public boolean stopCellEditing() {
		try {
			spinner.commitEdit();
			return super.stopCellEditing();
		} catch (ParseException e) {
			DateEditor dateEditor = (JSpinner.DateEditor) spinner.getEditor();
			Logger.getLogger(JSpinnerTableCellEditor.class).debug("Failed to date " + dateEditor.getTextField().getText());
		}
		return false;
	}

}