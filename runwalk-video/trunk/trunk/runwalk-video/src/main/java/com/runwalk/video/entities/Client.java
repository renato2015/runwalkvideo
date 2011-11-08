package com.runwalk.video.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;

@Entity
@SuppressWarnings("serial")
@DiscriminatorValue(Client.PERSON_TYPE)
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name = "phppos_customers")
public class Client extends Person {
	
	public static final String LAST_ANALYSIS_DATE = "lastAnalysisDate";
	// discriminator value for customer
	public static final String PERSON_TYPE = "0";
	
	/**
	 * 'Synthetic' property to allow firing events when adding/removing analyses
	 */
	public static final String ANALYSIS_COUNT = "analysisCount";
	public static final String ORGANIZATION = "organization";
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "client")
	@JoinFetch(JoinFetchType.OUTER)
	private List<Analysis> analyses = new ArrayList<Analysis>();

	@Column(name = "account_number")
	private String taxNumber;
	
	@Transient
	private String organization;
	@Transient
	private Date lastAnalysisDate;

	public Client() {	}
	
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

	public int getAnalysesCount() {
		return getAnalyses().size();
	}
	
	public List<Analysis> getAnalyses() {
		return analyses;
	}

	public boolean removeAnalysis(Analysis analysis) {
		boolean result = false;
		if (analysis != null) {
			int oldSize = getAnalysesCount();
			result = getAnalyses().remove(analysis);
			firePropertyChange(ANALYSIS_COUNT, oldSize, getAnalysesCount());
			Date lastAnalysisDate = null;
			if (!getAnalyses().isEmpty()) {
				Analysis lastAnalysis = getAnalyses().get(getAnalysesCount() - 1);
				lastAnalysisDate = lastAnalysis.getCreationDate();
			}
			setLastAnalysisDate(lastAnalysisDate);
		}
		return result;
	}

	public boolean addAnalysis(Analysis analysis) {
		int oldSize = getAnalysesCount();
		boolean result = getAnalyses().add(analysis);
		firePropertyChange(ANALYSIS_COUNT, oldSize, getAnalysesCount());
		setLastAnalysisDate(analysis.getCreationDate());
		return result;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		firePropertyChange(ORGANIZATION, this.organization, this.organization = organization);
	}

	public Date getLastAnalysisDate() {
		if (lastAnalysisDate == null && !getAnalyses().isEmpty()) {
			Collections.sort(getAnalyses());
			lastAnalysisDate = getAnalyses().get(getAnalysesCount()-1).getCreationDate();
		}
		return lastAnalysisDate;
	}
	
	private void setLastAnalysisDate(Date lastAnalysisDate) {
		firePropertyChange(LAST_ANALYSIS_DATE, this.lastAnalysisDate, this.lastAnalysisDate = lastAnalysisDate);
	}
	

}
