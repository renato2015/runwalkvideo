package com.runwalk.video.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.City;
import com.runwalk.video.entities.Customer;

public class CustomerModel extends AbstractEntityModel<Customer> {
	
	public static final String NAME = "name";
	public static final String FIRSTNAME = "firstname";
	public static final String LAST_ANALYSIS_DATE = "lastAnalysisDate";
	public static final String ANALYSIS_COUNT = "analysisCount";
	public static final String ANALYSES = "analyses";
	public static final String CITY = "city";
	public static final String EMAIL_ADDRESS = "emailAddress";
	
	private Date lastAnalysisDate;
	
	private List<AnalysisModel> analysisModels = Lists.newArrayList();
	
	public CustomerModel(Customer customer, boolean update) {
		super(customer);
		if (update) {
			updateLastAnalysisDate();
		}
	}
	
	public CustomerModel(Customer customer) {
		this(customer, true);
	}
	
	public CustomerModel(Customer customer, Date lastAnalysisDate) {
		this(customer, false);
		this.lastAnalysisDate = lastAnalysisDate;
	}
	
	public boolean addAnalysisModel(AnalysisModel analysisModel) {
		int size = getAnalysesCount();
		Analysis analysis = analysisModel.getEntity();
		analysisModels.add(analysisModel);
		boolean result = getAnalyses().add(analysis);
		if (analysis.getFeedbackId() == null) {
			lastAnalysisDate = analysis.getStartDate();
		}
		firePropertyChange(ANALYSIS_COUNT, size, size + 1);
		return result;
	}
	
	public void addAnalysisModels(List<Analysis> analyses) {
		for (Analysis analysis : analyses) {
			if (getEntity().equals(analysis.getCustomer())) {
				analysis.setCustomer(getEntity());
				getAnalyses().add(analysis);
				analysisModels.add(new AnalysisModel(this, analysis));
			}
		}
		getEntity().setAnalyses(analyses);
	}
	
	public boolean removeAnalysisModel(AnalysisModel analysisModel) {
		boolean result = false;
		if (analysisModel != null) {
			int size = getAnalysesCount();
			result = analysisModels.remove(analysisModel);
			Analysis analysis = analysisModel.getEntity();
			result &= getAnalyses().remove(analysis);
			updateLastAnalysisDate();
			firePropertyChange(ANALYSIS_COUNT, size, size - 1);
		}
		return result;
	}

	private List<Analysis> getAnalyses() {
		return getEntity().getAnalyses();
	}
	
	private void updateLastAnalysisDate() {
		Date lastAnalysisDate = null;
		if (!getAnalyses().isEmpty()) {
			lastAnalysisDate = getLastAnalysis().getStartDate();
		}
		this.lastAnalysisDate = lastAnalysisDate;
	}
	
	private Analysis getLastAnalysis() {
		Collections.sort(getAnalyses());
		for (int i = getAnalysesCount()-1; i > 0; i--) {
			Analysis analysis = getAnalyses().get(i);
			if (analysis.getFeedbackId() == null) {
				return analysis;
			}
		}
		return Iterables.getLast(getAnalyses());
	}

	private int getAnalysesCount() {
		return getAnalysisModels().size();
	}
	
	public List<AnalysisModel> getAnalysisModels() {
		return analysisModels;
	}

	public Date getLastAnalysisDate() {
		return lastAnalysisDate;
	}
	
	public Long getId() {
		return getEntity().getId();
	}
	
	public String getName() {
		return getEntity().getName();
	}
	
	public String getFirstname() {
		return getEntity().getFirstname();
	}
	
	public void setName(String name) {
		firePropertyChange(NAME, getEntity().getName(), name);
		getEntity().setName(name);
	}
	
	public void setFirstname(String firstname) {
		firePropertyChange(FIRSTNAME, getEntity().getFirstname(), firstname);
		getEntity().setFirstname(firstname);
	}
	
	public void setCity(City city) {
		firePropertyChange(CITY, getEntity().getAddress().getCity(), city);
		getEntity().getAddress().setCity(city);
	}
	
	public City getCity() {
		return getEntity().getAddress().getCity();
	}
	
	public void setEmailAddress(String emailAddress) {
		firePropertyChange(EMAIL_ADDRESS, getEntity().getEmailAddress(), emailAddress);
		getEntity().setEmailAddress(emailAddress);
	}
	
	public String getEmailAddress() {
		return getEntity().getEmailAddress();
	}
	
	public String getOrganization() {
		return getEntity().getCompanyName();
	}

	@Override
	public void setEntity(Customer entity) {
		updateLastAnalysisDate();
		super.setEntity(entity);
	}

	@Override
	public String toString() {
		return getEntity().toString();
	}
	
}
