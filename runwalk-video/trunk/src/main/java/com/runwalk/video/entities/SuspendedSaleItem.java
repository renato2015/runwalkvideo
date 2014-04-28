package com.runwalk.video.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name="phppos_sales_suspended_items")
public class SuspendedSaleItem implements Serializable {
	
	public static final BigDecimal DEFAULT_DISCOUNT = BigDecimal.valueOf(10.0d);
	
	public static final int DEFAULT_QUANTITY = 1;

	@EmbeddedId
	private SuspendedSaleItemKey id;
	
	@Column(name="quantity_purchased")
	private int quantity;
	
	@Column(name="description")
	private String description;
	
	@Column(name="item_unit_price")
	private BigDecimal unitPrice;
	
	@Column(name="item_cost_price")
	private BigDecimal costPrice;
	
	@Column(name="discount_percent")
	private BigDecimal discountPercent = DEFAULT_DISCOUNT;
	
	public SuspendedSaleItem() { }

	public SuspendedSaleItem(SuspendedSale suspendedSale, Item item, Client client) {
		this(suspendedSale, item);
		quantity = DEFAULT_QUANTITY;
		costPrice = item.getCostPrice();
		unitPrice = item.getUnitPrice();
	}

	public SuspendedSaleItem(SuspendedSale suspendedSale, Item item) {
		id = new SuspendedSaleItemKey(suspendedSale, item);
	}

	public SuspendedSaleItemKey getId() {
		return id;
	}

	public void setId(SuspendedSaleItemKey id) {
		this.id = id;
	}
	
	public Long getSaleId() {
		return id.saleId;
	}
	
	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}

	public BigDecimal getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(BigDecimal discountPercent) {
		this.discountPercent = discountPercent;
	}
	
	public Long getItemId() {
		return id.itemId;
	}
	
	@Embeddable
	public static class SuspendedSaleItemKey implements Serializable {
		
		public SuspendedSaleItemKey() {	}

		public SuspendedSaleItemKey(SuspendedSale suspendedSale, Item item) {
			saleId = suspendedSale.getId();
			itemId = item.getId();
			line = suspendedSale.getSaleItems().size();
		}

		@Column(name="sale_id")
		private Long saleId;
		
		@Column(name="item_id")
		private Long itemId;
		
		@Column(name = "line")
		private int line;

		public Long getSaleId() {
			return saleId;
		}

		public void setSaleId(Long saleId) {
			this.saleId = saleId;
		}

		public Long getItemId() {
			return itemId;
		}

		public void setItemId(Long itemId) {
			this.itemId = itemId;
		}

		public int getLine() {
			return line;
		}

		public void setLine(int line) {
			this.line = line;
		}
		
	}
	
}
