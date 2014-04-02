package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@Table(name="phppos_subcategories")
public class ItemSubcategory implements Serializable {
	
	@Id
	private Long id;

	@ManyToOne
	@JoinColumn(name="category_id", nullable=false)
	private ItemCategory category;

	@Column(name="description")
	private String description;
	
	@Column(name="publish")
	private boolean publish;

	public Long getId() {
		return id;
	}

	public ItemCategory getCategory() {
		return category;
	}

	public String getDescription() {
		return this.description;
	}

	public boolean isPublish() {
		return publish;
	}
	
}
