package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@Table(name="ospos_items_categories")
public class ItemCategory implements Serializable {
	
	@Id
	@Column(name="id")
	private Long id;

	@Column(name="description")
	private String description;
	
	@Column(name="deleted")
	private boolean deleted;

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return this.description;
	}

	public boolean isDeleted() {
		return this.deleted;
	}

}
