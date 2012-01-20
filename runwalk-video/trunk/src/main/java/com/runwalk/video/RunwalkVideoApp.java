package com.runwalk.video;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.UndoableEditListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.utils.AppHelper;

import com.google.common.collect.Maps;
import com.runwalk.video.core.Containable;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.impl.JpaClientDao;
import com.runwalk.video.dao.impl.JpaDaoService;
import com.runwalk.video.entities.Client;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.media.MediaControls;
import com.runwalk.video.panels.AnalysisOverviewTablePanel;
import com.runwalk.video.panels.AnalysisTablePanel;
import com.runwalk.video.panels.ClientInfoPanel;
import com.runwalk.video.panels.ClientTablePanel;
import com.runwalk.video.panels.RedcordTablePanel;
import com.runwalk.video.panels.StatusPanel;
import com.runwalk.video.tasks.RefreshTask;
import com.runwalk.video.tasks.UploadLogFilesTask;
import com.runwalk.video.ui.AnalysisOverviewTableFormat;
import com.runwalk.video.ui.AnalysisTableFormat;
import com.runwalk.video.ui.AppInternalFrame;
import com.runwalk.video.ui.ClientTableFormat;
import com.runwalk.video.ui.RedcordTableFormat;
import com.runwalk.video.ui.VideoMenuBar;
import com.runwalk.video.ui.WindowManager;
import com.runwalk.video.ui.actions.ApplicationActionConstants;
import com.runwalk.video.ui.actions.ApplicationActions;
import com.runwalk.video.ui.actions.MediaActionConstants;
import com.runwalk.video.util.AppSettings;
import com.tomtessier.scrollabledesktop.JScrollableDesktopPane;

/**
 * The main class of the application.
 */
public class RunwalkVideoApp extends SingleFrameApplication implements ApplicationActionConstants, MediaActionConstants {

	private final static Logger LOGGER = Logger.getLogger(RunwalkVideoApp.class);

	private ClientTablePanel clientTablePanel;
	private StatusPanel statusPanel;
	private AnalysisTablePanel analysisTablePanel;
	private AnalysisOverviewTablePanel analysisOverviewTablePanel;
	private RedcordTablePanel redcordTablePanel;
	private ClientInfoPanel clientInfoPanel;
	private MediaControls mediaControls;
	private Containable clientMainView;
	private VideoMenuBar menuBar;
	private ApplicationActions applicationActions;
	private JScrollableDesktopPane scrollableDesktopPane;
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
	 * After logging has been set up, the application will launch using the swing application framework (SAF).
	 */
	public static void main(String[] args) {
		AppSettings.configureLog4j();
		LOGGER.log(Level.INFO, "Detected platform is " + AppHelper.getPlatform());
		launch(RunwalkVideoApp.class, args);
	}

	/*
	 * Application lifecycle methods
	 */

	/** {@inheritDoc} */
	@Override
	protected void ready() {
		// load data from the db using the BSAF task mechanism
		executeAction(getContext().getActionMap(), REFRESH_ACTION);
	}

	/** {@inheritDoc} */
	@Override
	protected void initialize(String[] args) {
		AppSettings.getInstance().loadSettings();
		String puName = getContext().getResourceMap().getString("Application.name");
		// read db connection properties from settings file
		Map<String, String> connectionProperties = Maps.newHashMap();
		connectionProperties.put("eclipselink.jdbc.url", AppSettings.getInstance().getDbUrl());
		connectionProperties.put("eclipselink.jdbc.user", AppSettings.getInstance().getDbUser());
		connectionProperties.put("eclipselink.jdbc.password", AppSettings.getInstance().getDbPassword());
		// create entityManagerFactory for default persistence unit
		EntityManagerFactory emFactory = Persistence.createEntityManagerFactory(puName, connectionProperties);
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
		applicationActions = new ApplicationActions();
		statusPanel = new StatusPanel();
		clientTablePanel = new ClientTablePanel(getVideoFileManager(), getDaoService());
		clientInfoPanel = new ClientInfoPanel(getClientTablePanel(), createUndoableEditListener());
		analysisTablePanel = new AnalysisTablePanel(getClientTablePanel(), createUndoableEditListener(), 
				AppSettings.getInstance(), getVideoFileManager(), getDaoService());
		analysisOverviewTablePanel = new AnalysisOverviewTablePanel(AppSettings.getInstance(), getVideoFileManager());
		redcordTablePanel = new RedcordTablePanel(getClientTablePanel(), createUndoableEditListener(), getDaoService());
		// create main desktop scrollpane
		scrollableDesktopPane = new JScrollableDesktopPane();
		getMainFrame().add(getScrollableDesktopPane());
		// create menu bar
		menuBar = new VideoMenuBar();
		// create window manager
		WindowManager windowManager = new WindowManager(getMenuBar(), getScrollableDesktopPane());
		// create mediaplayer controls
	//	List<String> classNames = AppSettings.getInstance().getVideoCapturerFactories();
	//	VideoCapturerFactory videoCapturerFactory = new CompositeVideoCapturerFactory(classNames);
		mediaControls = new MediaControls(AppSettings.getInstance(), getVideoFileManager(), 
				windowManager, getDaoService(), getAnalysisTablePanel(), getAnalysisOverviewTablePanel());
		mediaControls.startCapturer();
		// set tableformats for the two last panels
		clientTablePanel.setTableFormat(new ClientTableFormat(clientTablePanel.getResourceMap()));
		analysisTablePanel.setTableFormat(new AnalysisTableFormat(analysisTablePanel.getResourceMap()));
		analysisTablePanel.registerClickHandler(getMediaControls().getClickHandler());
		analysisOverviewTablePanel.setTableFormat(new AnalysisOverviewTableFormat(analysisOverviewTablePanel.getResourceMap()));
		analysisOverviewTablePanel.registerClickHandler(getMediaControls().getClickHandler());
		redcordTablePanel.setTableFormat(new RedcordTableFormat(redcordTablePanel.getResourceMap()));
		// create the main panel that holds customer and analysis controls & info
		clientMainView = createMainView();
		// add all internal frames from here!!!
		getMainFrame().setJMenuBar(getMenuBar());
		// add the window to the WINDOW menu
		windowManager.addWindow(getMediaControls());
		windowManager.addWindow(getClientMainView());
		// create a custom property to support saving internalframe sessions state
		getContext().getSessionStorage().putProperty(AppInternalFrame.class, new AppInternalFrame.InternalFrameProperty());
		// show the main frame, all its session settings from the last time will be restored
		show(getMainFrame());
	}

	@org.jdesktop.application.Action
	public void exitApplication() {
		exit();
	}

	@Override
	public void exit(EventObject event) {
		ResourceMap resourceMap = getContext().getResourceMap();
		int result = JOptionPane.showConfirmDialog(getMainFrame(), 
				resourceMap.getString("exitApplication.confirmDialog.text"), 
				resourceMap.getString("exitApplication.Action.text"), JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			//executeAction(getApplicationActionMap(), "uploadLogFiles");
			executeAction(getApplicationActionMap(), SAVE_SETTINGS_ACTION);
			if (isSaveNeeded()) {
				executeAction(getClientTablePanel().getApplicationActionMap(), SAVE_ENTITIES_ACTION);
			}
			executeAction(getMediaControls().getApplicationActionMap(), DISPOSE_VIDEO_COMPONENTS_ACTION);
			LOGGER.debug("Taskservice shutting down...");
			
			try {
				getContext().getTaskService().shutdown();
				getContext().getTaskService().awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			} finally {
				getDaoService().shutdown();
			}
			super.exit(event);
		}
	}

	/*
	 * Global application actions
	 */

	@org.jdesktop.application.Action
	public void saveSettings() {
		AppSettings.getInstance().saveSettings();
	}

	@org.jdesktop.application.Action(block = Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> refresh() {
		return new RefreshTask(getDaoService(), getClientTablePanel(), getAnalysisTablePanel(), 
				getAnalysisOverviewTablePanel(), getRedcordTablePanel());
	}

	@org.jdesktop.application.Action
	public Task<Void, Void> uploadLogFiles() {
		AppSettings appSettings = AppSettings.getInstance();
		return new UploadLogFilesTask(appSettings.getLogFile(), appSettings.getLogFileUploadUrl());
	}

	private Containable createMainView() {
		final JPanel panel = new JPanel();
		ResourceMap resourceMap = getContext().getResourceMap();
		panel.setName(resourceMap.getString("mainView.title"));
		// create the tabpanel
		JTabbedPane tabPanel = new  JTabbedPane();
		tabPanel.setName("detailTabbedPane");
		tabPanel.addTab(resourceMap.getString("clientInfoPanel.TabConstraints.tabTitle"),  getClientInfoPanel()); // NOI18N
		tabPanel.addTab(resourceMap.getString("analysisTablePanel.TabConstraints.tabTitle"),  getAnalysisTablePanel()); // NOI18N
		tabPanel.addTab(resourceMap.getString("analysisOverviewTablePanel.TabConstraints.tabTitle"),  getAnalysisOverviewTablePanel()); // NOI18N
		tabPanel.addTab(resourceMap.getString("redcordTablePanel.TabConstraints.tabTitle"),  getRedcordTablePanel()); // NOI18N
		// set layout and add everything to the frame
		panel.setLayout(new MigLayout("flowy", "[grow,fill]", "[grow,fill]"));
		panel.add(getClientTablePanel());
		panel.add(tabPanel, "height :280:");
		panel.add(getStatusPanel(), "height 30!");
		return new Containable() {

			public Component getComponent() {
				return panel;
			}

			public String getTitle() {
				return "Klanten & Analyses";
			}

			public boolean isResizable() {
				return true;
			}

		};
	}

	/*
	 * Getters and Setters for the main objects in this application
	 */

	public ApplicationActions getApplicationActions() {
		return applicationActions;
	}

	public Containable getClientMainView() {
		return clientMainView;
	}

	public VideoMenuBar getMenuBar() {
		return menuBar;
	}

	public JScrollableDesktopPane getScrollableDesktopPane() {
		return scrollableDesktopPane;
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
		return analysisOverviewTablePanel;
	}
	
	private RedcordTablePanel getRedcordTablePanel() {
		return redcordTablePanel;
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

	/**
	 * This method will look for an {@link Action} specified with the given key in the given {@link ActionMap} 
	 * and invoke its {@link Action#actionPerformed(ActionEvent)} method.
	 * 
	 * @param actionMap The {@link ActionMap} containing the {@link Action} to be executed
	 * @param actionKey The key of the {@link Action} to be executed
	 */
	public void executeAction(ActionMap actionMap, String actionKey) {
		Action action = actionMap.get(actionKey);
		if (action != null) {
			ActionEvent actionEvent = new ActionEvent(getMainFrame(), ActionEvent.ACTION_PERFORMED, actionKey);
			action.actionPerformed(actionEvent);
		}
	}

	private boolean isSaveNeeded() {
		return getClientTablePanel().isSaveNeeded();
	}

	private UndoableEditListener createUndoableEditListener() {
		return getApplicationActions().getUndoableEditListener();
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
