package com.runwalk.video.ui;

import org.jdesktop.application.ResourceMap;

import com.runwalk.video.model.ClientModel;

public class ClientModelTableFormat extends AbstractTableFormat<ClientModel> {

	public ClientModelTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
	}

	public Object getColumnValue(ClientModel clientModel, int column) {
		if(column == 0)      return clientModel.getId();
		else if(column == 1) return clientModel.getName();
		else if(column == 2) return clientModel.getFirstname();
		else if(column == 3) return clientModel.getLastAnalysisDate();
		throw new IllegalStateException();
	}

}
