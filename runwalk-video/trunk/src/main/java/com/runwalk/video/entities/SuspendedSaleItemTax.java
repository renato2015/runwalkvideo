package com.runwalk.video.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="phppos_suspended_sales_item_taxes")
public class SuspendedSaleItemTax {

	@EmbeddedId
	private SuspendedSaleItemTaxKey id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="percent")
	private Double percent;
	
	public SuspendedSaleItemTaxKey getId() {
		return id;
	}

	public void setId(SuspendedSaleItemTaxKey id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getPercent() {
		return percent;
	}

	public void setPercent(Double percent) {
		this.percent = percent;
	}

	@SuppressWarnings("serial")
	@Embeddable
	public static class SuspendedSaleItemTaxKey implements Serializable {
		
		@Column(name="item_id")
		private Long itemId;
		
		@Column(name = "sale_id")
		private Long saleId;
		
		@Column(name = "line")
		private String line;

		public Long getItemId() {
			return itemId;
		}

		public void setItemId(Long itemId) {
			this.itemId = itemId;
		}

		public Long getSaleId() {
			return saleId;
		}

		public void setSaleId(Long saleId) {
			this.saleId = saleId;
		}

		public String getLine() {
			return line;
		}

		public void setLine(String line) {
			this.line = line;
		}
		
	}

}
