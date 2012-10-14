package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@Table(name="prod_subcategory")
public class ProdSubcategory implements Serializable {
	@Id
	private int subcatid;

	@Column(name="fk_catid")
	private int fkCatid;

	@Column(name="subcatname")
	private String subcatname;

	@Column(name="ext_url")
	private String extUrl;

	protected ProdSubcategory() { }

	public int getSubcatid() {
		return this.subcatid;
	}

	public int getFkCatid() {
		return this.fkCatid;
	}

	public String getSubcatname() {
		return this.subcatname;
	}

	public String getExtUrl() {
		return this.extUrl;
	}

}
