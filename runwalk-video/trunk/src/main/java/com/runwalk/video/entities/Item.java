package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@Table(name = "phppos_items")
public class Item implements Serializable {
	
	@Id
	@Column(name="item_id")
	private Long id;
	
	@Column(name="item_number")
	private String itemNumber;
	
	@Column(name="name")
	private String name;
	
	@Column(name="description")
	private String description;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="subcategory_id", nullable=false )
	private ItemSubcategory subcategory;
	
	public Long getId() {
		return id;
	}

	public String getItemNumber() {
		return this.itemNumber;
	}

	public void setItemNumber(String itemNumber) {
		this.itemNumber = itemNumber;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ItemSubcategory getSubcategory() {
		return subcategory;
	}

	public void setSubcategory(ItemSubcategory subcategory) {
		this.subcategory = subcategory;
	}

	@Override
	public String toString() {
		return getName();
	}

}
