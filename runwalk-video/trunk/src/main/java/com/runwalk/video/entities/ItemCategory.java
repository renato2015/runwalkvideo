package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@Table(name="phppos_categories")
public class ItemCategory implements Serializable {
	
	@Id
	@Column(name="id")
	private Long id;

	@Column(name="name")
	private String name;
	
	@Column(name="publish")
	private boolean publish;

	public Long getId() {
		return id;
	}

	public String getName() {
		return this.name;
	}

	public boolean isPublish() {
		return this.publish;
	}

}
