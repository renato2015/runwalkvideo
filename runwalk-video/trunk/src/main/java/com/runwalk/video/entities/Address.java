package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.google.common.base.Objects;

@Embeddable
@SuppressWarnings("serial")
public class Address implements Serializable {
	
	@Column(name = "address_1")
	private String address;
	
	@ManyToOne
	@JoinColumn(name = "city_id")
	private City city;
	
	@Column(name = "zip")
	private String postalcode;
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public City getCity() {
		return city;
	}
	
	public void setCity(City city) {
		this.city = city;
	}
	
	public String getPostalcode() {
		return (postalcode == null && city != null) ? "" + city.getCode() : postalcode;
	}
	
	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
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
