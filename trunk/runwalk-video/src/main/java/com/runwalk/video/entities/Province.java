package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema="testdb", name="province")
public class Province implements Serializable {
	@Id
	private int id;

	private String name;

	private static final long serialVersionUID = 1L;

	protected Province() {
		super();
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

}
