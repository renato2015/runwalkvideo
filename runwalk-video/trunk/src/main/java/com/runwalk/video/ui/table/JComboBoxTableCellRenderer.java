package com.runwalk.video.ui.table;

import javax.swing.JComboBox;

public class JComboBoxTableCellRenderer extends JComponentTableCellRenderer<JComboBox> {

	public JComboBoxTableCellRenderer() {
		super(new JComboBox());
	}

	@Override
	protected JComboBox prepareComponent(JComboBox component, Object value) {
		// add the item to the comboxbox.
		component.removeAllItems();
		component.addItem(value);
		return component;
	}

}