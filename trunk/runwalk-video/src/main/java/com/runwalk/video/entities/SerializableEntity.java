package com.runwalk.video.entities;

import java.io.Serializable;
import org.jdesktop.application.AbstractBean;

@SuppressWarnings("serial")
public abstract class SerializableEntity<T> extends AbstractBean implements Comparable<T>, Serializable {

	private static final String DIRTY = "dirty";
	
	private boolean dirty;

	public abstract Long getId();

	public void setDirty(boolean b) {
		firePropertyChange(DIRTY, this.dirty, this.dirty = b);
	}
	
	public boolean isDirty() {
		return this.dirty;
	}	
	
}
