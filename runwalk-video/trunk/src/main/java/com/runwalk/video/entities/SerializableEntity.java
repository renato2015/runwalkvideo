package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.runwalk.video.core.PropertyChangeSupport;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class SerializableEntity<T> implements Comparable<T>, Serializable, PropertyChangeSupport {

	public static final String ID = "id";
	
	@Id
	@Column(name = ID)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return this.id;
	}
	
	protected void setId(Long id) {
		this.id = id;
	}
	
}
