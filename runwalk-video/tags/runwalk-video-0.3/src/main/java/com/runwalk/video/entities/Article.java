package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@Table(name = "articles")
@NamedQueries(value={
		@NamedQuery(name="findAllArticles", query="SELECT OBJECT(ar) from Article ar")
})
public class Article implements Serializable {
	@Id
	@Column(name="id")
	private int id;

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

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
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

}
