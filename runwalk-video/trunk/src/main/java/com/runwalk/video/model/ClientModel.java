package com.runwalk.video.model;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;

public class ClientModel extends AbstractEntityModel<Client> {
	
	public static final String NAME = "last_name";
	
	public static final String LAST_ANALYSIS_DATE = "lastAnalysisDate";
	
	public static final String ANALYSIS_COUNT = "analysisCount";
	
	private Date lastAnalysisDate;
	
	private List<AnalysisModel> analysisModels = Lists.newArrayList();
	
	public ClientModel(Client client) {
		super(client);
	}
	
	public ClientModel(Client client, Date lastAnalysisDate) {
		this(client);
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
				addAnalysisModel(new AnalysisModel(analysis));
			}
		}
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
	
	public String getOrganization() {
		return getEntity().getOrganization();
	}

}
