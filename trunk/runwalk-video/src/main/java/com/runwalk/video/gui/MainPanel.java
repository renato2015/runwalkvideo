package com.runwalk.video.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import com.runwalk.video.RunwalkVideoApp;

public class MainPanel extends MyInternalFrame {
	private static final long serialVersionUID = 1L;
	
	private JTabbedPane tabPanel;

	public MainPanel() {
		super(true);
		ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(RunwalkVideoApp.class);
		setTitle(resourceMap.getString("mainView.title"));
		setName(resourceMap.getString("mainView.title"));
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new AbsoluteLayout());
		contentPanel.add(RunwalkVideoApp.getApplication().getClientTablePanel(), new AbsoluteConstraints(10, 10, 580, 370));
		
		tabPanel = new  JTabbedPane();
		tabPanel.setName("detailTabbedPane");
		tabPanel.addTab(resourceMap.getString("infoPanel.TabConstraints.tabTitle"), RunwalkVideoApp.getApplication().getClientInfoPanel()); // NOI18N
		tabPanel.addTab(resourceMap.getString("analysisPanel.TabConstraints.tabTitle"), RunwalkVideoApp.getApplication().getAnalysisTablePanel()); // NOI18N
		tabPanel.addTab(resourceMap.getString("conversionPanel.TabConstraints.tabTitle"), RunwalkVideoApp.getApplication().getAnalysisOverviewTable()); // NOI18N
/*		tabPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tabPanel.getSelectedIndex() == 1) {
					RunwalkVideoApp.getApplication().getClientTableModel().saveSelectedItem();
				}
				else if (tabPanel.getSelectedIndex() == 2) {
					RunwalkVideoApp.getApplication().getAnalysisTableModel().saveItemList();
				}
			}
			
		});*/
		contentPanel.add(tabPanel, new AbsoluteConstraints(0, 380, 590, 280));
		
		setLayout(new BorderLayout());
		add(contentPanel, BorderLayout.CENTER);
		add(RunwalkVideoApp.getApplication().getStatusPanel(), BorderLayout.SOUTH);
	}


}
