package com.runwalk.video.panels;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.runwalk.video.core.AppComponent;

@SuppressWarnings("serial")
@AppComponent
public class AbstractPanel extends JPanel {

	public static final String DIRTY = "dirty";
	
	private Boolean dirty = Boolean.FALSE;

	public AbstractPanel() {
		super();
	}

	public AbstractPanel(LayoutManager layout) {
		super(layout);
	}
	
	/**
	 * Persist the panel's dirty state. Override this method if you need to do something special to 
	 * have the entities' state persisted to the database.
	 * @return <code>true</code> if saving succeeded
	 */
	public boolean save() {
		return true;
	}

	public Boolean isDirty() {
		return dirty;
	}

	public void setDirty(Boolean dirty) {
		firePropertyChange(DIRTY, this.dirty, this.dirty = dirty);
	}
	
	

}
