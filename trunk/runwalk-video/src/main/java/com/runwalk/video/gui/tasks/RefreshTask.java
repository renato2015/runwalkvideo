package com.runwalk.video.gui.tasks;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
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
			setProgress(0, 0, 2);
			final Query query = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createNamedQuery("findAllClients"); // NOI18N
			query.setHint("eclipselink.join-fetch", "c.analyses.recording");
			query.setHint("eclipselink.left-join-fetch", "c.city");
			message("fetchMessage");
			setProgress(1, 0, 2);
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					EventList<Client> clientList = GlazedLists.threadSafeList(GlazedLists.eventList(query.getResultList()));
					ClientTablePanel clientTablePanel = RunwalkVideoApp.getApplication().getClientTablePanel();
					clientTablePanel.setItemList(clientList, Client.class);
					//Create the list for the analyses
					final ListEventPublisher publisher = clientList.getPublisher();
					final ReadWriteLock readWriteLock = clientList.getReadWriteLock();
					EventList<Client> observedClients = clientTablePanel.getItemList();
					final CollectionList<Client, Analysis> unfinishedBusiness = new CollectionList<Client, Analysis>(observedClients, new CollectionList.Model<Client, Analysis>() {

						public List<Analysis> getChildren(Client parent) {
							EventList<Analysis> eventList = new BasicEventList(publisher, readWriteLock);
							eventList.addAll(parent.getAnalyses());
							return new FilterList<Analysis>(eventList, new Matcher<Analysis>() {

								public boolean matches(Analysis item) {
									return item.getRecording() != null && !item.getRecording().isCompressed();
								}
								
							});
						}
						
					});
					RunwalkVideoApp.getApplication().getAnalysisOverviewTable().setItemList(unfinishedBusiness, Analysis.class);
					//Create the overview with unfinished analyses
					final EventList<Client> selectedClients = clientTablePanel.getEventSelectionModel().getSelected();
					CollectionList<Client, Analysis> analyses = new CollectionList<Client, Analysis>(selectedClients, new CollectionList.Model<Client, Analysis>() {

						public List<Analysis> getChildren(Client parent) {
							return parent.getAnalyses();
						}

					});
					RunwalkVideoApp.getApplication().getAnalysisTablePanel().setItemList(analyses, Analysis.class);
				}
				
			});
			
			
			setProgress(2, 0, 2);
			message("endMessage");
		} catch(Exception ignore) {
			errorMessage(ignore.getLocalizedMessage());
			success = false;
		}
		return success;
	}
	
	public class ListCollectionListModel<E, S> implements CollectionList.Model<List<E>,S> {
	    public List<S> getChildren(List<E> parent) {
	        return Collections.emptyList();
	    }
	}
}