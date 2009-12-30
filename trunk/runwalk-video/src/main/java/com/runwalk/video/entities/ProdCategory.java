package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="prod_category", schema = "testdb")
public class ProdCategory implements Serializable {
	@Id
	private int catid;

	private String catname;

	private byte publish;

	private static final long serialVersionUID = 1L;

	protected ProdCategory() { }

	public int getCatid() {
		return this.catid;
	}

	public void setCatid(int catid) {
		this.catid = catid;
	}

	public String getCatname() {
		return this.catname;
	}

	public byte getPublish() {
		return this.publish;
	}

}
