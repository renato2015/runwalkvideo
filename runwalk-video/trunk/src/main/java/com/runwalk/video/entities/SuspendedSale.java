package com.runwalk.video.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@SuppressWarnings("serial")
@Table(name="ospos_sales_suspended")
public class SuspendedSale implements Serializable {
	
	@Id
	@Column(name="sale_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long saleId;

	@Column(name="sale_time")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date saleTime = new Date();
	
	@ManyToOne
	@JoinColumn(name="customer_id", nullable=false )
	private Customer customer;
	
	@Column(name="employee_id")
	private Long employeeId;
	
	@OneToMany
	@JoinColumn(name="sale_id")
	private List<SuspendedSaleItem> saleItems;
	
	@OneToMany
	@JoinColumn(name="sale_id")
	private List<SuspendedSaleItemTax> saleItemTaxes;
	
	@Column(name="comment")
	private String comment = "";
	
	public SuspendedSale() {}

	public SuspendedSale(Customer customer, Long employeeId) {
		this.customer = customer;
		this.employeeId = employeeId;
	}
	
	public Long getId() {
		return saleId;
	}
	
	public Long getEmployeeId() {
		return employeeId;
	}

	public Date getSaleTime() {
		return saleTime;
	}

	public void setSaleTime(Date saleTime) {
		this.saleTime = saleTime;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public List<SuspendedSaleItem> getSaleItems() {
		return saleItems;
	}

	public void setSaleItems(List<SuspendedSaleItem> saleItems) {
		this.saleItems = saleItems;
	}
	
	public List<SuspendedSaleItemTax> getSaleItemTaxes() {
		return saleItemTaxes;
	}

	public void setSaleItemTaxes(List<SuspendedSaleItemTax> saleItemTaxes) {
		this.saleItemTaxes = saleItemTaxes;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
}
