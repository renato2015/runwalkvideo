package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.jdesktop.application.AbstractBean;

@Embeddable
public class Address extends AbstractBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Column(name = "address1")
	private String address;
	
	@ManyToOne
	@JoinColumn(name = "cityid")
	private City city;
	
	@Column(name = "postalCode")
	private String postalcode;
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.firePropertyChange("address", this.address, this.address = address);
	}
	
	public City getCity() {
		return city;
	}
	
	public void setCity(City city) {
		this.firePropertyChange("city", this.city, this.city = city);
	}
	
	public String getPostalcode() {
		return (postalcode == null && city != null) ? "" + city.getCode() : postalcode;
	}
	
	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}
	
	
	
}
