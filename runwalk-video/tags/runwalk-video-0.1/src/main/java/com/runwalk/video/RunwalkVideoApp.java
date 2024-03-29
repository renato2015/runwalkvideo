package com.runwalk.video;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.UndoableEditListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.application.utils.AppHelper;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.impl.JpaClientDao;
import com.runwalk.video.dao.impl.JpaDaoService;
import com.runwalk.video.entities.Client;
import com.runwalk.video.gui.AnalysisOverviewTableFormat;
import com.runwalk.video.gui.AnalysisTableFormat;
import com.runwalk.video.gui.AppInternalFrame;
import com.runwalk.video.gui.AppWindowWrapper;
import com.runwalk.video.gui.VideoMenuBar;
import com.runwalk.video.gui.actions.ApplicationActions;
import com.runwalk.video.gui.media.MediaControls;
import com.runwalk.video.gui.panels.AnalysisOverviewTablePanel;
import com.runwalk.video.gui.panels.AnalysisTablePanel;
import com.runwalk.video.gui.panels.ClientInfoPanel;
import com.runwalk.video.gui.panels.ClientTablePanel;
import com.runwalk.video.gui.panels.StatusPanel;
import com.runwalk.video.gui.tasks.RefreshTask;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.util.AppSettings;
import com.tomtessier.scrollabledesktop.BaseInternalFrame;
import com.tomtessier.scrollabledesktop.JScrollableDesktopPane;

/**
 * The main class of the application.
 */
public class RunwalkVideoApp extends SingleFrameApplication {

	private final static Logger LOGGER = Logger.getLogger(RunwalkVideoApp.class);

	private ClientTablePanel clientTablePanel;
	private StatusPanel statusPanel;
	private AnalysisTablePanel analysisTablePanel;
	private AnalysisOverviewTablePanel overviewTablePanel;
	private ClientInfoPanel clientInfoPanel;
	private MediaControls mediaControls;
	private AppInternalFrame clientMainView;
	private VideoMenuBar menuBar;
	private ApplicationActions applicationActions;
	private JScrollableDesktopPane pane;
	private VideoFileManager videoFileManager;
	private DaoService daoService;

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

	/** {@inheritDoc} */
	@Override
	protected void initialize(String[] args) {
		AppSettings.getInstance().loadSettings();
		String puName = getContext().getResourceMap().getString("Application.name");
		// create entityManagerFactory for default persistence unit
		EntityManagerFactory emFactory = Persistence.createEntityManagerFactory(puName);
		// create specialized dao's
		JpaClientDao clientDao = new JpaClientDao(Client.class, emFactory);
		// create daoManager and add specialized dao's
		daoService = new JpaDaoService(emFactory);
		getDaoService().addDao(clientDao);
		// create video file manager
		videoFileManager = new VideoFileManager(AppSettings.getInstance());
	}

	/**
	 * Initialize and show the application GUI.
	 */
	protected void startup() {
		// create common application actions class
		applicationActions = new ApplicationActions(AppSettings.getInstance());
		statusPanel = new StatusPanel();
		clientTablePanel = new ClientTablePanel(getVideoFileManager(), getDaoService());
		clientInfoPanel = new ClientInfoPanel(getClientTablePanel(), createUndoableEditListener());
		analysisTablePanel = new AnalysisTablePanel(getClientTablePanel(), createUndoableEditListener(), 
				AppSettings.getInstance(), getVideoFileManager(), getDaoService());
		overviewTablePanel = new AnalysisOverviewTablePanel(AppSettings.getInstance(), getVideoFileManager());
		// create main desktop scrollpane
		pane = new JScrollableDesktopPane();
		getMainFrame().add(pane);
		// create menu bar
		menuBar = new VideoMenuBar();
		// create mediaplayer controls
		mediaControls = new MediaControls(getAnalysisTablePanel(), AppSettings.getInstance(), getVideoFileManager(), getDaoService());
		mediaControls.startCapturer();
		// set tableformats for the two last panels
		analysisTablePanel.setTableFormat(new AnalysisTableFormat(getMediaControls()));
		overviewTablePanel.setTableFormat(new AnalysisOverviewTableFormat(getMediaControls()));
		// create the main panel that holds customer and analysis controls & info
		clientMainView = createMainView();
		// add all internal frames from here!!!
		getMainFrame().setJMenuBar(getMenuBar());
		// add the window to the WINDOW menu
		createOrShowComponent(getMediaControls());
		createOrShowComponent(getClientMainView());
		// create a custom property to support saving internalframe sessions state
		getContext().getSessionStorage().putProperty(AppInternalFrame.class, new AppInternalFrame.InternalFrameProperty());
		// show the main frame, all its session settings from the last time will be restored
		show(getMainFrame());
	}
	
	@org.jdesktop.application.Action
	public void saveSettings() {
		AppSettings.getInstance().saveSettings();
	}
	
	@org.jdesktop.application.Action(block = Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> refresh() {
		RefreshTask refreshTask = new RefreshTask(getDaoService());
		refreshTask.addTaskListener(new TaskListener.Adapter<Boolean, Void>() {

			@Override
			public void finished(TaskEvent<Void> event) {
				// refresh video file cache, this task should not be launched until the database data is refreshed
				executeAction(getAnalysisOverviewTablePanel().getApplicationActionMap(), "refreshVideoFiles");
			}
			
		});
		return refreshTask;
	}

	/** {@inheritDoc} */
	@Override
	protected void ready() {
		// load data from the db using the bsaf task mechanism
		executeAction(getContext().getActionMap(), "refresh");
	}
	
	/**
	 * This method will look for an {@link Action} specified with the given key in the given {@link ActionMap} 
	 * and invoke its {@link Action#actionPerformed(ActionEvent)} method.
	 * 
	 * @param actionMap The {@link ActionMap} containing the {@link Action} to be executed
	 * @param actionKey The key of the {@link Action} to be executed
	 */
	private void executeAction(ActionMap actionMap, String actionKey) {
		Action action = actionMap.get(actionKey);
		if (action != null) {
			ActionEvent actionEvent = new ActionEvent(getMainFrame(), ActionEvent.ACTION_PERFORMED, actionKey);
			action.actionPerformed(actionEvent);
		}
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

	public void removeComponent(AppWindowWrapper appComponent) {
		getMenuBar().removeWindow(appComponent);
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
		super.shutdown();
		saveSettings();
//		executeAction(getApplicationActionMap(), "uploadLogFiles");
		if (isSaveNeeded()) {
			int result = JOptionPane.showConfirmDialog(getMainFrame(), 
					"Wilt u de gemaakte wijzigingen opslaan?", 
					"Wijzingen bewaren..", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				executeAction(getClientTablePanel().getApplicationActionMap(), "save");
			}
		}
		getContext().getTaskService().shutdown();
		while(!getContext().getTaskService().isTerminated()) {
			Thread.yield();
		}
		getDaoService().shutdown();
	}

	private AppInternalFrame createMainView() {
		AppInternalFrame result = new AppInternalFrame("Klanten en analyses", true);
		ResourceMap resourceMap = getContext().getResourceMap();
		result.setName(resourceMap.getString("mainView.title"));
		// create the tabpanel
		JTabbedPane tabPanel = new  JTabbedPane();
		tabPanel.setName("detailTabbedPane");
		tabPanel.addTab(resourceMap.getString("infoPanel.TabConstraints.tabTitle"),  getClientInfoPanel()); // NOI18N
		tabPanel.addTab(resourceMap.getString("analysisPanel.TabConstraints.tabTitle"),  getAnalysisTablePanel()); // NOI18N
		tabPanel.addTab(resourceMap.getString("conversionPanel.TabConstraints.tabTitle"),  getAnalysisOverviewTablePanel()); // NOI18N
		// set layout and add everything to the frame
		result.setLayout(new MigLayout("flowy", "[grow,fill]", "[grow,fill]"));
		result.add(getClientTablePanel());
		result.add(tabPanel, "height :280:");
		result.add(getStatusPanel(), "height 30!");
		return result;
	}

	/*
	 * Getters & Setters for the main objects in this program
	 */

	public ApplicationActions getApplicationActions() {
		return applicationActions;
	}

	public AppInternalFrame getClientMainView() {
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
		return analysisTablePanel;
	}

	public AnalysisOverviewTablePanel getAnalysisOverviewTablePanel() {
		return overviewTablePanel;
	}

	public ClientInfoPanel getClientInfoPanel() {
		return clientInfoPanel;
	}

	public MediaControls getMediaControls() {
		return mediaControls;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	private DaoService getDaoService() {
		return daoService;
	}

	/*
	 * Convenience methods
	 */

	private boolean isSaveNeeded() {
		return getClientTablePanel().isSaveNeeded();
	}

	private UndoableEditListener createUndoableEditListener() {
		return getApplicationActions().getUndoableEditListener();
	}

	public void showMessage(String msg) {
		getStatusPanel().showMessage(msg);
	}

	public void showError(String error) {
		Toolkit.getDefaultToolkit().beep();
		getStatusPanel().showErrorMessage(error);
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
