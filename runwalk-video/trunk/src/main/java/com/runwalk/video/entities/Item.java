package com.runwalk.video.entities;

import java.io.Serializable;
import java.util.Objects;

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
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="item_size_id")
	private ItemSize itemSize;
	
	public Long getId() {
		return id;
	}

	public String getItemNumber() {
		return this.itemNumber;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public ItemSubcategory getSubcategory() {
		return subcategory;
	}
	
	public ItemSize getItemSize() {
		return itemSize;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			Item other = (Item) obj;
			return Objects.equals(getItemNumber(), other.getItemNumber())
				&& Objects.equals(getId(), other.getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getItemNumber(), getId());
	}

	@Override
	public String toString() {
		return getDescription() + " " + getItemSize().getSize();
	}

}
