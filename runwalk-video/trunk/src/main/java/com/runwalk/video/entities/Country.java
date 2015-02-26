package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@SuppressWarnings("serial")
@Embeddable
public class Country implements Serializable {
	
	@Column(name="country")
	private String name;
	
	public String getName() {
		return name;
	}
	
	
}
