package com.runwalk.video.ui.table;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class JCheckBoxTableCellRenderer extends JCheckBox implements
		TableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected,
			boolean hasFocus,
			int row, int column) {
		if(value instanceof Component ) {
			return (Component)value;
		} 
		if (value instanceof Boolean) {
			// set the button's enabled state
			setSelected((Boolean) value);
		}
		return this;
	}
	
	@Override
	public void revalidate() { }

	@Override
	public void repaint(long tm, int x, int y, int width, int height) { }
	
}
