package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@Table(name="phppos_states")
public class State implements Serializable {
	@Id
	private int id;

	private String name;

	protected State() {	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
