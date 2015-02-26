package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@SuppressWarnings("serial")
@Embeddable
public class State implements Serializable {
	
	@Column(name="state")
	private String name;
	
	@Embedded
	private Country country = new Country();
	
	protected State() {	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Country getCountry() {
		if (country == null) {
			country = new Country();
		}
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
	
}
