package com.runwalk.video.tasks;

import java.awt.Robot;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.jdesktop.application.Task;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.google.common.collect.Sets;
import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.jpa.AnalysisDao;
import com.runwalk.video.dao.jpa.CustomerDao;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.City;
import com.runwalk.video.entities.Customer;
import com.runwalk.video.glazedlists.LazyCollectionModel;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.model.AnalysisModel;
import com.runwalk.video.model.CustomerModel;
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
	private final AbstractTablePanel<CustomerModel> customerTablePanel;
	private final AnalysisTablePanel analysisTablePanel;
	private final VideoFileManager videoFileManager;

	public RefreshTask(DaoService daoService, AbstractTablePanel<CustomerModel> customerTablePanel, AnalysisTablePanel analysisTablePanel,
			VideoFileManager videoFileManager) {
		super("refresh");
		this.daoService = daoService;
		this.customerTablePanel = customerTablePanel;
		this.analysisTablePanel = analysisTablePanel;
		this.videoFileManager = videoFileManager;
	}

	protected Boolean doInBackground() {
		boolean success = true;
		try {
			message("startMessage");
			// get all cities from the db
			Set<City> allCities = Sets.newHashSet(getDaoService().getDao(City.class).getAll());
			final EventList<City> cityList = GlazedLists.eventList(allCities);
			// get all customers from the db
			CustomerDao customerDao = getDaoService().getDao(Customer.class);
			List<CustomerModel> customerModels = customerDao.getAllAsModels();
			final EventList<CustomerModel> customerList = GlazedLists.eventList(customerModels);
			//final EventList<Item> articleList = GlazedLists.eventList(allArticles);
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					RunwalkVideoApp.getApplication().getCustomerInfoPanel().setItemList(cityList);
					// get customer table panel and inject data
					getCustomerTablePanel().setItemList(customerList);
					final EventList<CustomerModel> selectedCustomers = getCustomerTablePanel().getEventSelectionModel().getSelected();
					
					final AnalysisDao analysisDao = daoService.getDao(Analysis.class);
					CollectionList<CustomerModel, AnalysisModel> selectedCustomerAnalyses = new CollectionList<CustomerModel, AnalysisModel>(selectedCustomers, 
							new LazyCollectionModel<Customer, Analysis, CustomerModel, AnalysisModel>(getTaskService(), CustomerModel.ANALYSES) {
						
						public List<AnalysisModel> getLoadedChildren(CustomerModel customerModel) {
							return customerModel.getAnalysisModels();
						}
						
                        public List<Analysis> loadChildren(Customer customer) {
                        	List<Analysis> analysesByCustomer = analysisDao.getAnalysesByCustomer(customer);
                        	getVideoFileManager().refreshCache(analysesByCustomer);
							return analysesByCustomer;
                        }
                       
                        public void refreshParent(CustomerModel customerModel, List<Analysis> analyses) {
                        	try {
                        		customerList.getReadWriteLock().writeLock().lock();
                        		customerModel.addAnalysisModels(analyses);
                        		getCustomerTablePanel().getObservableElementList().elementChanged(customerModel);
                        	} finally {
                        		customerList.getReadWriteLock().writeLock().unlock();
                        	}
                        }

                    });
					getAnalysisTablePanel().setItemList(selectedCustomerAnalyses, customerTablePanel);
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
	
	private AbstractTablePanel<CustomerModel> getCustomerTablePanel() {
		return customerTablePanel;
	}

	private AnalysisTablePanel getAnalysisTablePanel() {
		return analysisTablePanel;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}
	
}