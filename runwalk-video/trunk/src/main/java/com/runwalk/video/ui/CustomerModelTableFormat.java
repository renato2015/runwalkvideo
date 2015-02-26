package com.runwalk.video.ui;

import org.jdesktop.application.ResourceMap;

import com.runwalk.video.model.CustomerModel;

public class CustomerModelTableFormat extends AbstractTableFormat<CustomerModel> {

	public CustomerModelTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
	}

	public Object getColumnValue(CustomerModel customerModel, int column) {
		if(column == 0)      return customerModel.getId();
		else if(column == 1) return customerModel.getName();
		else if(column == 2) return customerModel.getFirstname();
		else if(column == 3) return customerModel.getLastAnalysisDate();
		throw new IllegalStateException();
	}

}
