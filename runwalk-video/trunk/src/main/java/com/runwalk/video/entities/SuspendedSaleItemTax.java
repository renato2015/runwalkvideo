package com.runwalk.video.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="phppos_sales_suspended_items_taxes")
public class SuspendedSaleItemTax {

	public static final String DEFAULT_VAT_NAME = "VAT";
	
	public static final BigDecimal DEFAULT_VAT_PCT = BigDecimal.valueOf(21d);

	@EmbeddedId
	private SuspendedSaleItemTaxKey id;
	
	@Column(name="name")
	private String name = DEFAULT_VAT_NAME;
	
	@Column(name="percent")
	private BigDecimal percent = DEFAULT_VAT_PCT;
	
	public SuspendedSaleItemTax() {	}
	
	public SuspendedSaleItemTax(SuspendedSaleItemTaxKey suspendedSaleItemKey) {
		id = suspendedSaleItemKey;
	}
	
	public SuspendedSaleItemTax(Item item, SuspendedSale suspendedSale) {
		this(new SuspendedSaleItemTaxKey(item, suspendedSale));
	}	

	public SuspendedSaleItemTax(String name, BigDecimal percent) {
		this.name = name;
		this.percent = percent;
	}

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

	public BigDecimal getPercent() {
		return percent;
	}

	public void setPercent(BigDecimal percent) {
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
		
		public SuspendedSaleItemTaxKey() { }
		
		public SuspendedSaleItemTaxKey(Item item, SuspendedSale suspendedSale) {
			itemId = item.getId();
			saleId = suspendedSale.getId();
			line = Integer.toString(suspendedSale.getSaleItems().size());
		}

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
