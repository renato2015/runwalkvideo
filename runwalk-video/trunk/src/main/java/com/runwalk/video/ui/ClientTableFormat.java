package com.runwalk.video.ui;

import org.jdesktop.application.ResourceMap;

import com.runwalk.video.model.ClientModel;

public class ClientTableFormat extends AbstractTableFormat<ClientModel> {

	public ClientTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
	}

	public Object getColumnValue(ClientModel client, int column) {
		if(column == 0)      return client.getId();
		else if(column == 1) return client.getName();
		else if(column == 2) return client.getFirstname();
		else if(column == 3) return client.getLastAnalysisDate();
		throw new IllegalStateException();
	}

}
