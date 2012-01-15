package com.runwalk.video.ui.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.runwalk.video.util.AppSettings;

public class JComponentTableCellRenderer<V extends Component> implements TableCellRenderer {
	
	private Class<?> rendererClass;
	
	private V component;

	public JComponentTableCellRenderer(Class<?> rendererClass) {
		this.rendererClass = rendererClass;
		this.component = null;
	}

	public JComponentTableCellRenderer(V component) {
		this.rendererClass = component.getClass();
		this.component = component;
		component.setFont(AppSettings.MAIN_FONT);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected,
			boolean hasFocus,
			int row, int column) {
		if(value instanceof Component ) {
			return (Component)value;
		} else if ((table.getModel().isCellEditable(row, column)) && component != null) {
			Component component = prepareComponent(this.component, value);
			component.setBackground(table.getSelectionBackground());
			return component;
		}
		return table.getDefaultRenderer(rendererClass).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

	@SuppressWarnings("unused")
	protected Component prepareComponent(V component, Object value) {
		return component;
	}
}