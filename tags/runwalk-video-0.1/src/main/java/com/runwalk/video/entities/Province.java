package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@Table(schema="testdb", name="province")
public class Province implements Serializable {
	@Id
	private int id;

	private String name;

	protected Province() {	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

}
