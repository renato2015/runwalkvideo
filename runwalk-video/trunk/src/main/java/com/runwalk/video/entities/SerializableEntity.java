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
public abstract class SerializableEntity<T extends SerializableEntity<T>> implements Comparable<T>, Serializable, PropertyChangeSupport {

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
	
	public int compareTo(T other) {
		int result = 1;
		if (other != null) {
			result = equals(other) ? 0 : getId().compareTo(other.getId());
		}
		return result;
	}
	
}
