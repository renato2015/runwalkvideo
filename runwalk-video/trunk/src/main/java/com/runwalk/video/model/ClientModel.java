package com.runwalk.video.model;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.City;
import com.runwalk.video.entities.Client;

public class ClientModel extends AbstractEntityModel<Client> {
	
	public static final String NAME = "name";
	public static final String FIRSTNAME = "firstname";
	public static final String LAST_ANALYSIS_DATE = "lastAnalysisDate";
	public static final String ANALYSIS_COUNT = "analysisCount";
	public static final String ANALYSES = "analyses";
	public static final String CITY = "city";
	
	private Date lastAnalysisDate;
	
	private List<AnalysisModel> analysisModels = Lists.newArrayList();
	
	public ClientModel(Client client, boolean update) {
		super(client);
		if (update) {
			updateLastAnalysisDate();
		}
	}
	
	public ClientModel(Client client) {
		this(client, true);
	}
	
	public ClientModel(Client client, Date lastAnalysisDate) {
		this(client, false);
		this.lastAnalysisDate = lastAnalysisDate;
	}
	
	public boolean addAnalysisModel(AnalysisModel analysisModel) {
		int size = getAnalysesCount();
		Analysis analysis = analysisModel.getEntity();
		analysisModels.add(analysisModel);
		boolean result = getAnalyses().add(analysis);
		firePropertyChange(ANALYSIS_COUNT, size, size + 1);
		lastAnalysisDate = analysis.getCreationDate();
		return result;
	}
	
	public void addAnalysisModels(List<Analysis> analyses) {
		for (Analysis analysis : analyses) {
			if (getEntity().equals(analysis.getClient())) {
				analysis.setClient(getEntity());
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
			Analysis analysis = analysisModel.getEntity();
			result = getAnalyses().remove(analysis);
			if (result) {
				firePropertyChange(ANALYSIS_COUNT, size, size - 1);
				updateLastAnalysisDate();
			}
		}
		return result;
	}

	private List<Analysis> getAnalyses() {
		return getEntity().getAnalyses();
	}
	
	private void updateLastAnalysisDate() {
		Date lastAnalysisDate = null;
		if (!getAnalyses().isEmpty()) {
			lastAnalysisDate = getLastAnalysis().getCreationDate();
		}
		this.lastAnalysisDate = lastAnalysisDate;
	}
	
	private Analysis getLastAnalysis() {
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
	
	public String getOrganization() {
		return getEntity().getOrganization();
	}

	@Override
	public void setEntity(Client entity) {
		updateLastAnalysisDate();
		super.setEntity(entity);
	}

	@Override
	public String toString() {
		return getEntity().toString();
	}
	
}
