package com.runwalk.video.entities;

import java.io.Serializable;
import org.jdesktop.application.AbstractBean;

@SuppressWarnings("serial")
public abstract class SerializableEntity<T> extends AbstractBean implements Comparable<T>, Serializable {

	private boolean dirty;

	public abstract Long getId();

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}	
	
}
