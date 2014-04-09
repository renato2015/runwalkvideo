package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name="phppos_items_sizes")
public class ItemSize implements Serializable {

	@Id
	@Column(name="id")
	private Long id;
	
	@Column(name="size")
	private String size;

	public Long getId() {
		return id;
	}

	public String getSize() {
		return size;
	}
	
}
