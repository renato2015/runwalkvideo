package com.runwalk.video.gui.tasks;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
import com.runwalk.video.gui.ClientTablePanel;

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
			query.setHint("eclipselink.left-join-fetch", "c.analyses.recording");
			query.setHint("eclipselink.left-join-fetch", "c.city");
			message("fetchMessage");
			setProgress(1, 0, 2);
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					EventList<Client> clientList = GlazedLists.threadSafeList(GlazedLists.eventList(query.getResultList()));
					ClientTablePanel clientTablePanel = RunwalkVideoApp.getApplication().getClientTablePanel();
					clientTablePanel.setItemList(clientList, Client.class);

					final EventList<Client> selectedClients = clientTablePanel.getEventSelectionModel().getSelected();
					CollectionList<Client, Analysis> analyses = new CollectionList<Client, Analysis>(selectedClients, new CollectionList.Model<Client, Analysis>() {

						@Override
						public List<Analysis> getChildren(Client parent) {
							return parent.getAnalyses();
						}

					});
					RunwalkVideoApp.getApplication().getAnalysisTablePanel().setItemList(analyses, Analysis.class);
					final ListEventPublisher publisher = clientList.getPublisher();
					final ReadWriteLock readWriteLock = clientList.getReadWriteLock();
					final CollectionList<Client, Analysis> unfinishedBusiness = new CollectionList<Client, Analysis>(clientList, new CollectionList.Model<Client, Analysis>() {

						@Override
						public List<Analysis> getChildren(Client parent) {
							EventList<Analysis> eventList = new BasicEventList(publisher, readWriteLock);
							eventList.addAll(parent.getAnalyses());
//							GlazedLists.syncEventListToList(eventList, parent.getAnalyses());
							return new FilterList<Analysis>(eventList, new Matcher<Analysis>() {

								public boolean matches(Analysis item) {
									return item.getRecording() != null && !item.getRecording().isCompressed();
								}
								
							});
						}
						
					});
					unfinishedBusiness.getPublisher();
					RunwalkVideoApp.getApplication().getAnalysisOverviewTable().setItemList(unfinishedBusiness, Analysis.class);
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