package com.runwalk.video.tasks;

import java.awt.Robot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;
import javax.swing.SwingUtilities;

import org.jdesktop.application.Task;
import org.jdesktop.application.TaskService;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.CompositeList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Article;
import com.runwalk.video.entities.City;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.RedcordSession;
import com.runwalk.video.entities.RedcordTableElement;
import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.panels.AbstractTablePanel;
import com.runwalk.video.panels.AnalysisTablePanel;
import com.runwalk.video.ui.AnalysisConnector;
import com.runwalk.video.ui.RedcordTableElementConnector;

/**
 * This {@link Task} handles all database lookups and injects the results in the appropriate application component.
 * 
 * @author Jeroen Peelaerts
 *
 */
public class RefreshTask extends AbstractTask<Boolean, Void> {

	private final DaoService daoService;
	private final AbstractTablePanel<Client> clientTablePanel;
	private final AnalysisTablePanel analysisTablePanel;
	private final AbstractTablePanel<Analysis> analysisOverviewTablePanel;
	private final AbstractTablePanel<RedcordTableElement> redcordTablePanel;

	public RefreshTask(DaoService daoService, AbstractTablePanel<Client> clientTablePanel, 
			AnalysisTablePanel analysisTablePanel, AbstractTablePanel<Analysis> analysisOverviewTablePanel, 
			AbstractTablePanel<RedcordTableElement> redcordTablePanel) {
		super("refresh");
		this.daoService = daoService;
		this.clientTablePanel = clientTablePanel;
		this.analysisTablePanel = analysisTablePanel;
		this.analysisOverviewTablePanel = analysisOverviewTablePanel;
		this.redcordTablePanel = redcordTablePanel;
	}

	protected Boolean doInBackground() {
		boolean success = true;
		try {
			message("startMessage");
			// get all clients from the db
			List<Client> allClients = getDaoService().getDao(Client.class).getAll();
			final EventList<Client> clientList = GlazedLists.eventList(allClients);
			// get all cities from the db
			List<City> allCities = getDaoService().getDao(City.class).getAll();
			final EventList<City> cityList = GlazedLists.eventList(allCities);
			// get all articles from the db
			List<Article> allArticles = getDaoService().getDao(Article.class).getAll();
			final EventList<Article> articleList = GlazedLists.eventList(allArticles);
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					RunwalkVideoApp.getApplication().getClientInfoPanel().setItemList(cityList);
					// get client table panel and inject data
					getClientTablePanel().setItemList(clientList, Client.class);
					final EventList<Client> selectedClients = getClientTablePanel().getEventSelectionModel().getSelected();
					final CollectionList.Model<Client, Analysis> model = new CollectionList.Model<Client, Analysis>() {

						public List<Analysis> getChildren(Client parent) {
							return parent.getAnalyses();
						}

					};
					CollectionList<Client, Analysis> selectedClientAnalyses = new CollectionList<Client, Analysis>(selectedClients, model);
					// get analysis tablepanel and inject data
					getAnalysisTablePanel().setArticleList(articleList);
					getAnalysisTablePanel().setItemList(selectedClientAnalyses, new AnalysisConnector());
					// create pipeline for overview tablepanel
					final EventList<Client> deselectedClients = getClientTablePanel().getEventSelectionModel().getDeselected();
					final CollectionList<Client, Analysis> deselectedClientAnalyses = new CollectionList<Client, Analysis>(deselectedClients, model);
					// get analysis overview tablepanel and inject data
					final CompositeList<Analysis> analysesOverview = new CompositeList<Analysis>(selectedClientAnalyses.getPublisher(), selectedClientAnalyses.getReadWriteLock());
					analysesOverview.addMemberList(selectedClientAnalyses);
					analysesOverview.addMemberList(deselectedClientAnalyses);
					// create the overview with unfinished analyses
					getAnalysisOverviewTablePanel().setItemList(analysesOverview, new AnalysisConnector());
					// create pipeline for redord tablepanel
					CollectionList<Client, RedcordTableElement> redcordTableElements = new CollectionList<Client, RedcordTableElement>(selectedClients, 
						new LazyModel<Client, RedcordTableElement>(getTaskService(), "redcordSessions") {

							public List<RedcordTableElement> getLazyChildren(Client parent) {
								ArrayList<RedcordTableElement> result = new ArrayList<RedcordTableElement>();
								for (RedcordSession redcordSession : parent.getRedcordSessions()) {
									result.addAll(redcordSession.getRedcordExercises());
									if (!redcordSession.isSynthetic()) {
										result.add(redcordSession);
									}
								}
								return result;
							}

							public void refreshParent(Client parent, List<RedcordTableElement> children) {
								getClientTablePanel().getObservableElementList().elementChanged(parent);
							}

					});
					getRedcordTablePanel().setItemList(redcordTableElements, new RedcordTableElementConnector());
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
	
	private AbstractTablePanel<Client> getClientTablePanel() {
		return clientTablePanel;
	}

	private AnalysisTablePanel getAnalysisTablePanel() {
		return analysisTablePanel;
	}

	private AbstractTablePanel<Analysis> getAnalysisOverviewTablePanel() {
		return analysisOverviewTablePanel;
	}

	private AbstractTablePanel<RedcordTableElement> getRedcordTablePanel() {
		return redcordTablePanel;
	}
	
	/**
	 * An extension of {@link CollectionList.Model} that will fetch a parent entity's property in a background thread.
	 *
	 * @param <E> The parent entity that owns the property collection to be loaded
	 * @param <S> The type of the child entity
	 */
	public static abstract class LazyModel<E extends SerializableEntity<? super E>, S> implements CollectionList.Model<E, S> {

		private final String attributeName;
		
		private final TaskService taskService;

		public LazyModel(TaskService taskService, String attributeName) {
			this.taskService = taskService;
			this.attributeName = attributeName;
		}
		
		public abstract List<S> getLazyChildren(E parent);
		
		public abstract void refreshParent(E parent, List<S> children);	
		
		public List<S> getChildren(final E parent) {
			List<S> result = Collections.emptyList();
			PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
			if (persistenceUtil.isLoaded(parent, getAttributeName())) {
				result = getLazyChildren(parent);
			} else {
				getTaskService().execute(new AbstractTask<List<S>, Void>("loadEntities") {
					
					protected List<S> doInBackground() throws Exception {
						List<S> result = getLazyChildren(parent);
						message("endMessage", parent);
						return result;
					}
					
					@Override
					protected void succeeded(List<S> children) {
						// execute callback and refresh the parent list
						refreshParent(parent, children);
					}
					
				});
			}
			return result;
		}

		public String getAttributeName() {
			return attributeName;
		}

		public TaskService getTaskService() {
			return taskService;
		}
		
	}
	
}