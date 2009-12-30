package com.runwalk.video.gui.actions;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
import com.runwalk.video.gui.AbstractTableModel;
import com.runwalk.video.gui.tasks.CleanupRecordingsTask;
import com.runwalk.video.gui.tasks.CompressTask;

public class TableActions extends AbstractBean {
	private boolean clientSelected, analysisSelected, compressionEnabled, cleanupEnabled = true;

	private final static Logger LOGGER = Logger.getLogger(TableActions.class);

	private ResourceMap map = RunwalkVideoApp.getApplication().getContext().getResourceMap(TableActions.class);

	@Action
	public void addClient() {
		Client client = new Client();
		AbstractTableModel.persistEntity(client);
		RunwalkVideoApp.getApplication().getClientTablePanel().clearSearch();
		int itemIndex = RunwalkVideoApp.getApplication().getClientTableModel().addItem(client);

		int tableIndex = RunwalkVideoApp.getApplication().getClientTable().convertRowIndexToView(itemIndex);
		RunwalkVideoApp.getApplication().getClientTablePanel().makeRowVisible(tableIndex);
		RunwalkVideoApp.getApplication().getClientInfoPanel().requestFocus();

		RunwalkVideoApp.getApplication().getAnalysisTableModel().update();
		RunwalkVideoApp.getApplication().setSaveNeeded(true);
	}


	@Action(enabledProperty = "clientSelected")
	public void deleteClient() {
		int n = JOptionPane.showConfirmDialog(
				RunwalkVideoApp.getApplication().getMainFrame(),
				map.getString("deleteClient.confirmDialog.text"), //$NON-NLS-1$
				map.getString("deleteClient.Action.text"), //$NON-NLS-1$
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION)	return;

		int selected = RunwalkVideoApp.getApplication().getClientTablePanel().getTable().getSelectedRow();
		Client selectedClient = RunwalkVideoApp.getApplication().getSelectedClient();
		RunwalkVideoApp.getApplication().getClientTableModel().deleteItem(selectedClient);
		AbstractTableModel.deleteEntity(selectedClient);

		if (selected > 0) {
			RunwalkVideoApp.getApplication().getClientTablePanel().makeRowVisible(selected - 1);
		}
		LOGGER.debug("Client " + selectedClient.getId() +  " (" + selectedClient.getName() + ") deleted."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		RunwalkVideoApp.getApplication().setSaveNeeded(true);
	}

	public boolean isClientSelected() {
		return clientSelected;
	}

	public void setClientSelected(boolean selected) {
		this.clientSelected = selected;
		this.firePropertyChange("clientSelected", !isClientSelected(), isClientSelected()); //$NON-NLS-1$
	}

	@Action(enabledProperty = "clientSelected")
	public void addAnalysis() {
		//insert a new analysis record
		Client selectedClient = RunwalkVideoApp.getApplication().getSelectedClient();
		Analysis analysis = new Analysis(selectedClient);
		AbstractTableModel.persistEntity(analysis);

		//update models ..
		selectedClient.addAnalysis(analysis);
		RunwalkVideoApp.getApplication().getClientTableModel().updateSelectedRow();
		int analysisRow = RunwalkVideoApp.getApplication().getAnalysisTableModel().addItem(analysis);
		int cameraRow = RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().addItem(analysis);

		//show newly inserted record
		RunwalkVideoApp.getApplication().getAnalysisTablePanel().makeRowVisible(analysisRow);
		RunwalkVideoApp.getApplication().getAnalysisOverviewTable().makeRowVisible(cameraRow);
//		RunwalkVideoApp.getApplication().getClientTableModel().updateTimestampCache();
		LOGGER.debug("Analysis " + analysis.getId() + " for client " + selectedClient.getId() + " (" + selectedClient.getName() +  ") added.");
		RunwalkVideoApp.getApplication().getPlayerGUI().captureFrameToFront();
	}

	@Action(enabledProperty = "analysisSelected")
	public void deleteAnalysis() {		
		if (RunwalkVideoApp.getApplication().getAnalysisTablePanel().isRowSelected()) {
			int n = JOptionPane.showConfirmDialog(
					RunwalkVideoApp.getApplication().getMainFrame(),
					map.getString("deleteAnalysis.confirmDialog.text"),
					map.getString("deleteAnalysis.Action.text"),
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.OK_CANCEL_OPTION);
			if (n == JOptionPane.CANCEL_OPTION ||n == JOptionPane.CLOSED_OPTION) return;
			Analysis selectedAnalysis = RunwalkVideoApp.getApplication().getSelectedAnalysis();

			RunwalkVideoApp.getApplication().getSelectedClient().removeAnalysis(selectedAnalysis);
			int analysisRow = RunwalkVideoApp.getApplication().getAnalysisTableModel().deleteItem(selectedAnalysis);
			int cameraRow = RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().deleteItem(selectedAnalysis);
			AbstractTableModel.deleteEntity(selectedAnalysis);

			//select previous records..
			if (analysisRow > 0) {
				RunwalkVideoApp.getApplication().getAnalysisTablePanel().makeRowVisible(analysisRow-1);
			} else {
				RunwalkVideoApp.getApplication().getAnalysisTableModel().clearItemSelection();
			}
			if (cameraRow > 0) {
				RunwalkVideoApp.getApplication().getAnalysisOverviewTable().makeRowVisible(cameraRow-1);
			} else {
				RunwalkVideoApp.getApplication().getAnalysisTableModel().clearItemSelection();
			}
//			RunwalkVideoApp.getApplication().getClientTableModel().updateTimestampCache();
			RunwalkVideoApp.getApplication().getClientTableModel().updateSelectedRow();
			LOGGER.debug("Analysis " + selectedAnalysis.getId() + " for client " + selectedAnalysis.getClient().getId() + " (" + selectedAnalysis.getClient().getName() + ") deleted."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		setCompressionEnabled(true);
	}


	public boolean isAnalysisSelected() {
		return analysisSelected;
	}

	public void setAnalysisSelected(boolean selected) {
		this.analysisSelected = selected;
		this.firePropertyChange("analysisSelected", !isAnalysisSelected(), isAnalysisSelected());
	}

	@Action
	public void updateOverview() {
		RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().update();
		setCompressionEnabled(true);
	}

	public boolean isCleanupEnabled() {
		return cleanupEnabled;
	}

	public void setCleanupEnabled(boolean cleanUpEnabled) {
		this.cleanupEnabled = cleanUpEnabled;
		this.firePropertyChange("cleanupEnabled", !isCleanupEnabled(), isCleanupEnabled());
	}

	@Action(enabledProperty="cleanupEnabled")
	public Task<Boolean, Void> cleanup() {
		return new CleanupRecordingsTask();
	}

	@Action(enabledProperty = "compressionEnabled", block=Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> compress() {
		setCompressionEnabled(false);
		return new CompressTask();
	}

	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(boolean b) {
		compressionEnabled = b ? b && RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().isCompressionEnabled() : false;
		firePropertyChange("compressionEnabled", !isCompressionEnabled(), isCompressionEnabled());
	}

}
