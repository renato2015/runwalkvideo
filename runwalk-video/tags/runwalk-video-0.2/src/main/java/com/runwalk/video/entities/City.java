package com.runwalk.video.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "phppos_cities")
@NamedQueries(value={
		@NamedQuery(name="findByZipCode", query="SELECT DISTINCT c from City c WHERE c.code = :zipCode ORDER BY c.code ASC"),
		@NamedQuery(name="findByName", query="SELECT DISTINCT c from City c WHERE c.name = :name ORDER BY c.code ASC"),
		@NamedQuery(name="findAllCities", query="SELECT DISTINCT c from City c ORDER BY c.code ASC")
})
public class City extends SerializableEntity<City> {
	
	@Column(name="code")
	private Integer code;

	@Column(name="name")
	private String name;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private State state;

	public City() {	}
	
	public City(int code, String name) {
		super();
		this.name = name;
		this.code = code;
	}

	public Integer getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}
	
	public State getState() {
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
			result &= getId() != null ? getId().equals(other.getId()) : result;
		}
		return result;
	}

	public int compareTo(City arg0) {
		return this.equals(arg0) ? 0 : getCode().compareTo(arg0.getCode());
	}
	
}
