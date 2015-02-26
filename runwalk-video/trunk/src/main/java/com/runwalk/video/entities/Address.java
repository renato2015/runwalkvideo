package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.google.common.base.Objects;

@Embeddable
@SuppressWarnings("serial")
public class Address implements Serializable {
	
	@Column(name = "address_1")
	private String address;
	
	@Embedded
	private City city = new City();
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public City getCity() {
		if (city == null) {
			this.city = new City();
		}
		return city;
	}
	
	public void setCity(City city) {
		this.city = city;
	}
	
	public String getPostalcode() {
		return city.getCode();
	}
	
	public void setPostalcode(String postalcode) {
		city.setCode(postalcode);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			Address other = (Address) obj;
			return Objects.equal(getAddress(), other.getAddress()) &&
				Objects.equal(getPostalcode(), other.getPostalcode()) &&
				Objects.equal(getCity(), other.getCity());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getPostalcode(), getAddress(), getCity());
	}
	
	
	
	
}
