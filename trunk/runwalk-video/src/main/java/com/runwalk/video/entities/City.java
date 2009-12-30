package com.runwalk.video.entities;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@SuppressWarnings("serial")
@Entity
@Table(schema="testdb", name = "city")
public class City implements Serializable {
    @Transient
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	@Id
	@Column(name="id")
	private Long id;

	@Column(name="code")
	private Integer code;

	@Column(name="name")
	private String name;

	protected City() {
		super();
	}
	
	public City(int code, String name) {
		super();
		this.name = name;
		this.code = code;
	}

	public Long getId() {
		return this.id;
	}

	public Integer getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return getCode() + " " + getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass().equals(obj.getClass())) {
			City city = (City) obj;
			result = city.getCode() == this.getCode() && city.getName().equals(getName());
		}
		return result;
	}

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
	
}
