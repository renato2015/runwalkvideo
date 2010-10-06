package com.runwalk.video.entities;

import java.io.Serializable;

import com.runwalk.video.gui.PropertyChangeSupport;

@SuppressWarnings("serial")
public abstract class SerializableEntity<T> implements Comparable<T>, Serializable, PropertyChangeSupport {

	private boolean dirty;

	public abstract Long getId();

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}	
	
}
