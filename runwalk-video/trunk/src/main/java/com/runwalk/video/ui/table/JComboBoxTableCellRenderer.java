package com.runwalk.video.ui.table;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.runwalk.video.settings.SettingsManager;

@SuppressWarnings("serial")
public class JComboBoxTableCellRenderer extends JComboBox implements TableCellRenderer {
	
	public JComboBoxTableCellRenderer() {
		setFont(SettingsManager.MAIN_FONT);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected,
			boolean hasFocus,
			int row, int column) {
		if(value instanceof Component ) {
			return (Component)value;
		} else if ((table.getModel().isCellEditable(row, column))) {
			// add the item to the comboxbox.
			removeAllItems();
			addItem(value);
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
			return this;
		}
		return table.getDefaultRenderer(getClass()).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
	
	@Override
	public void repaint(long tm, int x, int y, int width, int height) { }

	@Override
	public void revalidate() { }

}