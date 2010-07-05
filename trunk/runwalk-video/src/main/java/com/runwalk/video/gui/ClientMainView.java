package com.runwalk.video.gui;

import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

public class ClientMainView extends AppInternalFrame {
	
	public ClientMainView() {
		//TODO haal naam van de frame uit resourceMap
		super("Klanten en analyses", true);
		setName(getResourceMap().getString("mainView.title"));
		
		JTabbedPane tabPanel = new  JTabbedPane();
		tabPanel.setName("detailTabbedPane");
		tabPanel.addTab(getResourceMap().getString("infoPanel.TabConstraints.tabTitle"),  getApplication().getClientInfoPanel()); // NOI18N
		tabPanel.addTab(getResourceMap().getString("analysisPanel.TabConstraints.tabTitle"),  getApplication().getAnalysisTablePanel()); // NOI18N
		tabPanel.addTab(getResourceMap().getString("conversionPanel.TabConstraints.tabTitle"),  getApplication().getAnalysisOverviewTable()); // NOI18N
		
		setLayout(new MigLayout("flowy"));
		add(getApplication().getClientTablePanel(), "width :590:");
		add(tabPanel, "height :280:, width :590:");
		add(getApplication().getStatusPanel(), "width :590:");
	}


}
