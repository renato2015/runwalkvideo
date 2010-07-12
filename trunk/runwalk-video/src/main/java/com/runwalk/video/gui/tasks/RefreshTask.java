package com.runwalk.video.gui.tasks;

import java.util.List;
import java.util.logging.Level;

import javax.persistence.Query;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.CompositeList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
import com.runwalk.video.gui.AnalysisConnector;
import com.runwalk.video.gui.panels.AnalysisOverviewTablePanel;
import com.runwalk.video.gui.panels.AnalysisTablePanel;
import com.runwalk.video.gui.panels.ClientTablePanel;

public class RefreshTask extends AbstractTask<Boolean, Void> {

	public RefreshTask() {
		super("refresh");
	}

	@Override 
	@SuppressWarnings("unchecked")
	protected Boolean doInBackground() {
		boolean success = true;
		try {
			message("startMessage");
//			setProgress(0, 0, 3);
			final Query query = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createNamedQuery("findAllClients"); // NOI18N
			query.setHint("eclipselink.left-join-fetch", "c.analyses.recording");
			message("fetchMessage");
//			setProgress(1, 0, 3);
//			query.setHint("eclipselink.left-join-fetch", "c.address.city");
			final List<Client> resultList = query.getResultList();
//			setProgress(2, 0, 3);
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					EventList<Client> clientList = GlazedLists.threadSafeList(GlazedLists.eventList(resultList));
					ClientTablePanel clientTablePanel = RunwalkVideoApp.getApplication().getClientTablePanel();
					clientTablePanel.setItemList(clientList, Client.class);
					//Create the list for the analyses
					final EventList<Client> selectedClients = clientTablePanel.getEventSelectionModel().getSelected();
					CollectionList<Client, Analysis> selectedClientAnalyses = new CollectionList<Client, Analysis>(selectedClients, new CollectionList.Model<Client, Analysis>() {

						public List<Analysis> getChildren(Client parent) {
							return parent.getAnalyses();
						}

					});
					AnalysisTablePanel analysisTablePanel = RunwalkVideoApp.getApplication().getAnalysisTablePanel();
					analysisTablePanel.setItemList(selectedClientAnalyses, new AnalysisConnector());
					final EventList<Client> deselectedClients = clientTablePanel.getEventSelectionModel().getDeselected();
					final CollectionList<Client, Analysis> deselectedClientAnalyses = new CollectionList<Client, Analysis>(deselectedClients, new CollectionList.Model<Client, Analysis>() {

						public List<Analysis> getChildren(Client parent) {
							return parent.getAnalyses();
						}
						
					});
					
					final CompositeList<Analysis> analysesOverview = new CompositeList<Analysis>(selectedClientAnalyses.getPublisher(), selectedClientAnalyses.getReadWriteLock());
					analysesOverview.addMemberList(selectedClientAnalyses);
					analysesOverview.addMemberList(deselectedClientAnalyses);
					
					//Create the overview with unfinished analyses
					AnalysisOverviewTablePanel analysisOverviewTablePanel = RunwalkVideoApp.getApplication().getAnalysisOverviewTable();
					analysisOverviewTablePanel.setItemList(analysesOverview, new AnalysisConnector());
				}
				
			});
//			setProgress(3, 0, 3);
			message("endMessage");
		} catch(Exception ignore) {
			getLogger().error(Level.SEVERE, ignore);
			success = false;
		}
		return success;
	}

}