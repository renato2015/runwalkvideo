package com.runwalk.video.ui.table;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class JComboBoxTableCellRenderer<T> extends JComboBox<T> implements TableCellRenderer {
	
	private Class<T> contentType;
	
	public JComboBoxTableCellRenderer(Class<T> contentType)
	{
		this.contentType = contentType;
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
			addItem(contentType.cast(value));
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