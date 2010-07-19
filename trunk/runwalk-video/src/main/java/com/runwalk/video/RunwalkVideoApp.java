package com.runwalk.video;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
import com.runwalk.video.gui.AppInternalFrame;
import com.runwalk.video.gui.AppWindowWrapper;
import com.runwalk.video.gui.ClientMainView;
import com.runwalk.video.gui.VideoMenuBar;
import com.runwalk.video.gui.actions.ApplicationActions;
import com.runwalk.video.gui.media.MediaControls;
import com.runwalk.video.gui.panels.AnalysisOverviewTablePanel;
import com.runwalk.video.gui.panels.AnalysisTablePanel;
import com.runwalk.video.gui.panels.ClientInfoPanel;
import com.runwalk.video.gui.panels.ClientTablePanel;
import com.runwalk.video.gui.panels.StatusPanel;
import com.runwalk.video.util.AppSettings;
import com.tomtessier.scrollabledesktop.BaseInternalFrame;
import com.tomtessier.scrollabledesktop.JScrollableDesktopPane;

import de.humatic.dsj.DSEnvironment;

/**
 * The main class of the application.
 */
public class RunwalkVideoApp extends SingleFrameApplication {

	private final static Logger LOGGER = Logger.getLogger(RunwalkVideoApp.class);

	private  ClientTablePanel clientTablePanel;
	private  StatusPanel statusPanel;
	private  AnalysisTablePanel analysisPanel;
	private  AnalysisOverviewTablePanel overviewPanel;
	private  ClientInfoPanel clientInfoPanel;
	private MediaControls mediaControls;
	private ClientMainView clientMainView;

	private VideoMenuBar menuBar;

	private ApplicationActions applicationActions;

	private EntityManagerFactory emFactory = null;

	private JScrollableDesktopPane pane;

	/**
	 * A convenient static getter for the application instance.
	 * @return the instance of RunwalkVideoApp
	 */
	public static RunwalkVideoApp getApplication() {
		return Application.getInstance(RunwalkVideoApp.class);
	}

	/*
	 * Main method launching the application. 
	 * After loggin has been set up, the application will launch using the swing application framework (SAF).
	 */
	public static void main(String[] args) {
		AppSettings.configureLog4j();
		LOGGER.log(Level.INFO, "Detected platform is " + AppHelper.getPlatform());
		launch(RunwalkVideoApp.class, args);
	}

	private void loadUIState() throws IOException {
		getContext().getSessionStorage().putProperty(AppInternalFrame.class, new AppInternalFrame.InternalFrameProperty());
		getContext().getSessionStorage().restore(getMainFrame(), "desktopPane.xml");
		getContext().getSessionStorage().restore(getMediaControls(), "controlFrame.xml");
		getContext().getSessionStorage().restore(getClientMainView(), "mainFrame.xml");
	}

	private void saveUIState() throws IOException {
		LOGGER.log(Level.INFO, "Saving UI state to directory " + getContext().getLocalStorage().getDirectory());
		getContext().getSessionStorage().save(getMainFrame(), "desktopPane.xml");
		if (getMediaControls() != null) {
			getContext().getSessionStorage().save(getMediaControls(), "controlFrame.xml");
		}
		getContext().getSessionStorage().save(getClientMainView(), "mainFrame.xml");
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize(String[] args) {
		AppSettings.getInstance().loadSettings();
		String puName = getContext().getResourceMap().getString("Application.name");
		emFactory = Persistence.createEntityManagerFactory(puName);
	}

	/**
	 * Initialize and show the application GUI.
	 */
	protected void startup() {
		//the actions that the user can undertake?
		applicationActions = new ApplicationActions();

		statusPanel = new StatusPanel();
		clientTablePanel = new ClientTablePanel();
		clientInfoPanel = new  ClientInfoPanel();
		analysisPanel = new  AnalysisTablePanel();
		setSaveNeeded(false);
		overviewPanel = new AnalysisOverviewTablePanel();    	

		pane = new JScrollableDesktopPane();
		getMainFrame().add(pane);

		menuBar = new VideoMenuBar();

		mediaControls = new MediaControls();
		mediaControls.startCapturer();
		clientMainView = new ClientMainView();
		
		//add all internal frames from here!!!
		getMainFrame().setJMenuBar(getMenuBar());
		
		//add the window to the WINDOW menu
		createOrShowComponent(getMediaControls());
		createOrShowComponent(getClientMainView());

		show(getMainFrame());
		
		try {
			loadUIState();
		} catch (IOException e) {
			LOGGER.error("Failed to load UI state", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void ready() {
		// load data from the db using the bsaf task mechanism
		Action refreshAction = clientTablePanel.getApplicationActionMap().get("refresh");
		ActionEvent actionEvent = new ActionEvent (getMainFrame(), ActionEvent.ACTION_PERFORMED, "refresh");
		refreshAction.actionPerformed (actionEvent);
	}

	public void createOrShowComponent(AppWindowWrapper appComponent) {
		Container container = appComponent == null ? null : appComponent.getHolder();
		if (container != null) {
			container.setVisible(true);
			if (container instanceof BaseInternalFrame) {
				BaseInternalFrame baseInternalFrame = (BaseInternalFrame) container;
				if (new Dimension(0,0).equals(baseInternalFrame.getSize())) {
					baseInternalFrame.pack();
					pane.add(baseInternalFrame);
					getMenuBar().addWindow(appComponent);	
				}
				pane.enableAssociatedComponents(baseInternalFrame, true);
			}  else {
				getMenuBar().addWindow(appComponent);	
			}
		}
	}
	
	public void hideComponent(AppWindowWrapper appComponent) {
		Container container = appComponent == null ? null : appComponent.getHolder();
		if (container != null) {
			if (container instanceof BaseInternalFrame) {
				BaseInternalFrame baseInternalFrame = (BaseInternalFrame) container;
				pane.enableAssociatedComponents(baseInternalFrame, false);
			}
			container.setVisible(false);
		}
	}

	/** {@inheritDoc} */
	@Override 
	protected void shutdown() {
		Action uploadLogFilesAction = getApplicationActionMap().get("uploadLogFiles");
		ActionEvent actionEvent = new ActionEvent (getMainFrame(), ActionEvent.ACTION_PERFORMED, "uploadLogFiles");
		uploadLogFilesAction.actionPerformed (actionEvent);
		if (isSaveNeeded()) {
			int result = JOptionPane.showConfirmDialog(getMainFrame(), 
					"Wilt u de gemaakte wijzigingen opslaan?", 
					"Wijzingen bewaren..", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				Task<?, Void> saveTask = getClientTablePanel().save();
				getContext().getTaskService().execute(saveTask);
			}
		}
		AppSettings.getInstance().saveSettings();
		try {
			saveUIState();
			getContext().getTaskService().shutdown();
			while(!getContext().getTaskService().isTerminated()) {
				Thread.yield();
			}
		} catch (IOException e) {
			LOGGER.error("Failed to save UI state", e);
		} finally {
			emFactory.close();
			super.shutdown();
		}
	}

	//Getters & Setters for the main objects in this program

	public ApplicationActions getApplicationActions() {
		return applicationActions;
	}
	
	public ClientMainView getClientMainView() {
		return clientMainView;
	}

	public VideoMenuBar getMenuBar() {
		return menuBar;
	}

	public ClientTablePanel getClientTablePanel() {
		return clientTablePanel;
	}

	public StatusPanel getStatusPanel() {
		return statusPanel;
	}

	public AnalysisTablePanel getAnalysisTablePanel() {
		return analysisPanel;
	}

	public AnalysisOverviewTablePanel getAnalysisOverviewTable() {
		return overviewPanel;
	}

	public ClientInfoPanel getClientInfoPanel() {
		return clientInfoPanel;
	}

	public MediaControls getMediaControls() {
		return mediaControls;
	}

	//some convenience methods

	public boolean isSaveNeeded() {
		return getClientTablePanel().isSaveNeeded();
	}

	public void setSaveNeeded(boolean saveNeeded) {
		getClientTablePanel().setSaveNeeded(saveNeeded);
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return emFactory;
	}

	public Query createQuery(String query) {
		return getEntityManagerFactory().createEntityManager().createQuery(query)
		.setHint("toplink.refresh", "true")
		.setHint("oracle.toplink.essentials.config.CascadePolicy", "CascadePrivateParts ");
	}

	public Query createNativeQuery(String query) {
		return getEntityManagerFactory().createEntityManager().createNativeQuery(query);
	}

	public void showMessage(String msg) {
		getStatusPanel().showMessage(msg);
	}

	public void showError(String error) {
		Toolkit.getDefaultToolkit().beep();
		getStatusPanel().showErrorMessage(error);
	}

	public JTable getClientTable() {
		return getClientTablePanel().getTable();
	}

	public JTable getAnalysisTable() {
		return getAnalysisTablePanel().getTable();
	}

	public Client getSelectedClient() {
		return getClientTablePanel().getSelectedItem();
	}

	public Analysis getSelectedAnalysis() {
		return getAnalysisTablePanel().getSelectedItem();
	}

	public void clearStatusMessage() {
		getStatusPanel().showMessage("");
	}

	//getters for action maps in this application
	public ActionMap getActionMap(Object obj) {
		return getContext().getActionMap(obj);
	}

	public ActionMap getApplicationActionMap() {
		return getActionMap(getApplicationActions());
	}

	public static class RunwalkLogger extends org.eclipse.persistence.logging.AbstractSessionLog {

		public void log(org.eclipse.persistence.logging.SessionLogEntry arg0) {
			LOGGER.log(Level.toLevel(arg0.getLevel()), arg0.getMessage());
		}

	}

}
