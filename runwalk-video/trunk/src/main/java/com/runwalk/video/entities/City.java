package com.runwalk.video.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class City implements Comparable<City> {
	
	@Column(name="zip")
	private String code;

	@Column(name="city")
	private String name;

	@Embedded
	private State state = new State();

	public City() {	
		this("", "");
	}
	
	public City(String code, String name) {
		this.name = name;
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}
	
	public void setCode(String code) {
		this.code = code;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setState(State state) {
		this.state = state;
	}

	public State getState() {
		if (state == null) {
			state = new State();
		}
		return state;
	}

	@Override
	public String toString() {
		return getCode() + " " + getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCode() == null) ? 0 : getCode().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			City other = (City) obj;
			result = getCode() != null ? getCode().equals(other.getCode()) : other.getCode() == null;
			result &= getName() != null ? getName().equals(other.getName()) : other.getName() == null;
		}
		return result;
	}

	public int compareTo(City arg0) {
		return this.equals(arg0) ? 0 : getCode().compareTo(arg0.getCode());
	}
	
}
