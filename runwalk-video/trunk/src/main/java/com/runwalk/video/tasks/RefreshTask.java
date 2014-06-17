package com.runwalk.video.tasks;

import java.awt.Robot;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.jdesktop.application.Task;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.DebugList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.jpa.AnalysisDao;
import com.runwalk.video.dao.jpa.ClientDao;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.City;
import com.runwalk.video.entities.Client;
import com.runwalk.video.glazedlists.LazyCollectionModel;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.model.AnalysisModel;
import com.runwalk.video.model.ClientModel;
import com.runwalk.video.panels.AbstractTablePanel;
import com.runwalk.video.panels.AnalysisTablePanel;

/**
 * This {@link Task} handles all database lookups and injects the results in the appropriate application component.
 * 
 * @author Jeroen Peelaerts
 *
 */
public class RefreshTask extends AbstractTask<Boolean, Void> {

	private final DaoService daoService;
	private final AbstractTablePanel<ClientModel> clientTablePanel;
	private final AnalysisTablePanel analysisTablePanel;
	private final VideoFileManager videoFileManager;

	public RefreshTask(DaoService daoService, AbstractTablePanel<ClientModel> clientTablePanel, AnalysisTablePanel analysisTablePanel,
			VideoFileManager videoFileManager) {
		super("refresh");
		this.daoService = daoService;
		this.clientTablePanel = clientTablePanel;
		this.analysisTablePanel = analysisTablePanel;
		this.videoFileManager = videoFileManager;
	}

	protected Boolean doInBackground() {
		boolean success = true;
		try {
			message("startMessage");
			// get all cities from the db
			List<City> allCities = getDaoService().getDao(City.class).getAll();
			final EventList<City> cityList = GlazedLists.eventList(allCities);
			// get all clients from the db
			ClientDao clientDao = getDaoService().getDao(Client.class);
			List<ClientModel> clientModels = clientDao.getAllAsModels();
			final DebugList<ClientModel> clientList = new DebugList<ClientModel>();
			clientList.addAll(clientModels);
			clientList.setLockCheckingEnabled(true);
			//final EventList<Item> articleList = GlazedLists.eventList(allArticles);
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					RunwalkVideoApp.getApplication().getClientInfoPanel().setItemList(cityList);
					// get client table panel and inject data
					getClientTablePanel().setItemList(clientList);
					final EventList<ClientModel> selectedClients = getClientTablePanel().getEventSelectionModel().getSelected();
					
					final AnalysisDao analysisDao = daoService.getDao(Analysis.class);
					CollectionList<ClientModel, AnalysisModel> selectedClientAnalyses = new CollectionList<ClientModel, AnalysisModel>(selectedClients, 
							new LazyCollectionModel<Client, Analysis, ClientModel, AnalysisModel>(getTaskService(), ClientModel.ANALYSES) {
						
						public List<AnalysisModel> getLoadedChildren(ClientModel client) {
							return client.getAnalysisModels();
						}
						
                        public List<Analysis> loadChildren(Client client) {
                        	List<Analysis> analysesByClient = analysisDao.getAnalysesByClient(client);
                        	getVideoFileManager().refreshCache(analysesByClient);
							return analysesByClient;
                        }
                       
                        public void refreshParent(ClientModel clientModel, List<Analysis> analyses) {
                        	try {
                        		clientList.getReadWriteLock().writeLock().lock();
                        		clientModel.addAnalysisModels(analyses);
                        		getClientTablePanel().getObservableElementList().elementChanged(clientModel);
                        	} finally {
                        		clientList.getReadWriteLock().writeLock().unlock();
                        	}
                        }

                    });
					getAnalysisTablePanel().setItemList(selectedClientAnalyses, clientTablePanel);
				}

			});
			message("waitForIdleMessage");
			new Robot().waitForIdle();
			message("endMessage", getExecutionDuration(TimeUnit.SECONDS));
		} catch(Exception exc) {
			getLogger().error(Level.SEVERE, exc);
			success = false;
		}
		return success;
	}
	
	private DaoService getDaoService() {
		return daoService;
	}
	
	private AbstractTablePanel<ClientModel> getClientTablePanel() {
		return clientTablePanel;
	}

	private AnalysisTablePanel getAnalysisTablePanel() {
		return analysisTablePanel;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}
	
}