package com.runwalk.video.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@SuppressWarnings("serial")
@DiscriminatorValue(Client.PERSON_TYPE)
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "phppos_customers")
public class Client extends Person {
	
	// discriminator value for customer
	public static final String PERSON_TYPE = "0";
	
	public static final String ANALYSES = "analyses";
	/**
	 * 'Synthetic' property to allow firing events when adding/removing analyses
	 */
	public static final String ORGANIZATION = "organization";
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "client")
	private List<Analysis> analyses = new ArrayList<Analysis>();
	
	@Column(name = "account_number")
	private String taxNumber;

	@Transient
	private String organization;
	
	public Client() { }
	
	public Client(String name, String firstName) {
		setFirstname(firstName);
		setName(name);
	}
	
	public String getTaxNumber() {
		return this.taxNumber;
	}

	public void setTaxNumber(String taxNumber) {
		this.taxNumber = taxNumber;
	}
	
	public List<Analysis> getAnalyses() {
		return analyses;
	}
	
	public int getAnalysesCount() {
		return getAnalyses().size();
	}
	
	public boolean addAnalysis(Analysis analysis) {
		return getAnalyses().add(analysis);
	}
	
	public boolean removeAnalysis(Analysis analysis) {
		return getAnalyses().remove(analysis);
	}
	
	public void setAnalyses(List<Analysis> analyses) {
		this.analyses = analyses;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

}
