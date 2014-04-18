package com.runwalk.video.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="suspended_sale_item")
public class SuspendedSaleItem {

	@EmbeddedId
	private SuspendedSaleItemKey id;
	
	@OneToOne
	@JoinColumn(name="item_id")
	private Item item;
	
	@ManyToOne
	@JoinColumn(name="person_id")
	private Client client;
	
	@Column(name="quantity_purchased")
	private int quantity;
	
	@Column(name="line")
	private int line;
	
	@Column(name="sale_id")
	private Long saleId;
	
	@Column(name="description")
	private String description;
	
	@Column(name="item_unit_price")
	private BigDecimal unitPrice;
	
	@Column(name="item_cost_price")
	private BigDecimal costPrice;
	
	@Column(name="discount_percent")
	private int discountPercent;
	
	public SuspendedSaleItemKey getId() {
		return id;
	}

	public void setId(SuspendedSaleItemKey id) {
		this.id = id;
	}
	
	public void setSaleId(Long saleId) {
		this.saleId = saleId;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
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

	public int getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(int discountPercent) {
		this.discountPercent = discountPercent;
	}
	
	public Long getSaleId() {
		return id.saleId;
	}
	
	@Embeddable
	public static class SuspendedSaleItemKey implements Serializable {
		
		@Column(name="sale_id", updatable=false, insertable=false)
		private Long saleId;
		
		@Column(name="item_id", updatable=false, insertable=false)
		private Long itemId;
		
	}
	
}
