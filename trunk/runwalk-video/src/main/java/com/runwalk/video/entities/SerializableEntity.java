package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.runwalk.video.ui.PropertyChangeSupport;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class SerializableEntity<T> implements Comparable<T>, Serializable, PropertyChangeSupport {

	public static final String ID = "id";
	
	@Id
	@Column(name = ID)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Transient
	private boolean dirty;

	public Long getId() {
		return this.id;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}	
	
}
