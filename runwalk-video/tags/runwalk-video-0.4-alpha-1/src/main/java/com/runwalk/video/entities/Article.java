package com.runwalk.video.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@Table(name = "articles")
public class Article extends SerializableEntity<Article> {

	@Column(name="code")
	private String code;
	
	@Column(name="name")
	private String name;
	
	@Lob
	private String description;

	@Column(name="ext_url")
	private String extUrl;
	
	@Column(name="img")
	private String img;
	
	@Column(name="subcategory")
	private int subcategory;

	public Article() {
		super();
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
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

	public String getExtUrl() {
		return this.extUrl;
	}

	public void setExtUrl(String extUrl) {
		this.extUrl = extUrl;
	}

	public String getImg() {
		return this.img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public int getSubcategory() {
		return this.subcategory;
	}

	public void setSubcategory(int subcategory) {
		this.subcategory = subcategory;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Article o) {
		return getId().compareTo(o.getId());
	}

}
