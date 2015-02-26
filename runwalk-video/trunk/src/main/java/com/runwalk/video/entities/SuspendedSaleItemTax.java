package com.runwalk.video.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="ospos_sales_suspended_items_taxes")
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
	
	public SuspendedSaleItemTax(SuspendedSale suspendedSale, Item item) {
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
		
		@Column(name="item_id", nullable=false)
		private Long itemId;
		
		@Column(name = "sale_id", nullable=false)
		private Long saleId;
		
		@Column(name = "line")
		private int line;
		
		public SuspendedSaleItemTaxKey() { }
		
		public SuspendedSaleItemTaxKey(Item item, SuspendedSale suspendedSale) {
			itemId = item.getId();
			saleId = suspendedSale.getId();
			line = suspendedSale.getSaleItems().size();
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

		public int getLine() {
			return line;
		}

		public void setLine(int line) {
			this.line = line;
		}
		
	}

}
