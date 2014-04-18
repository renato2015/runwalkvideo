package com.runwalk.video.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="phppos_sales_suspended")
public class SuspendedSale {

	@Id
	@Column(name="sale_id")
	private Long id;
	
	@Column(name="sale_time")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date saleTime;
	
	@ManyToOne
	@JoinColumn(name="client")
	private Client client;
	
	@OneToMany
	@JoinColumn(name="sale_id")
	private List<SuspendedSaleItem> saleItems;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getSaleTime() {
		return saleTime;
	}

	public void setSaleTime(Date saleTime) {
		this.saleTime = saleTime;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public List<SuspendedSaleItem> getSaleItems() {
		return saleItems;
	}

	public void setSaleItems(List<SuspendedSaleItem> saleItems) {
		this.saleItems = saleItems;
	}
	
}
