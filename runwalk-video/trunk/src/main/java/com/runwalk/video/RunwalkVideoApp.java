package com.runwalk.video;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.utils.AppHelper;

import com.runwalk.video.core.Containable;
import com.runwalk.video.dao.CompositeDaoService;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.gdata.BaseEntryDaoService;
import com.runwalk.video.dao.jpa.JpaDaoService;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.media.MediaControls;
import com.runwalk.video.panels.AbstractPanel;
import com.runwalk.video.panels.AbstractTablePanel;
import com.runwalk.video.panels.AnalysisOverviewTablePanel;
import com.runwalk.video.panels.AnalysisTablePanel;
import com.runwalk.video.panels.ClientInfoPanel;
import com.runwalk.video.panels.ClientTablePanel;
import com.runwalk.video.panels.RedcordTablePanel;
import com.runwalk.video.panels.StatusPanel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.tasks.AbstractTask;
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
import com.runwalk.video.util.AWTExceptionHandler;
import com.tomtessier.scrollabledesktop.JScrollableDesktopPane;

/**
 * The main class of the application.
 */
public class RunwalkVideoApp extends SingleFrameApplication implements ApplicationActionConstants, MediaActionConstants {

	public static final String APP_VERSION = "Application.version";
	public static final String APP_TITLE = "Application.title";
	public static final String APP_NAME = "Application.name";
	public static final String APP_BUILD_DATE = "Application.build.date";

	private final static Logger LOGGER = Logger.getLogger(RunwalkVideoApp.class);
	
	private final static int MAIN_PANEL_MIN_HEIGHT = 600;

	private static final String SAVE_NEEDED = "saveNeeded";
	
	private static final String DIRTY = "dirty";
	
	private List<AbstractPanel> panels = new ArrayList<AbstractPanel>();
	private ClientTablePanel clientTablePanel;
	private AnalysisTablePanel analysisTablePanel;
	private AnalysisOverviewTablePanel analysisOverviewTablePanel;
	private RedcordTablePanel redcordTablePanel;
	private ClientInfoPanel clientInfoPanel;
	private MediaControls mediaControls;
	private Containable clientMainView;
	private VideoMenuBar menuBar;
	private StatusPanel statusPanel;
	private ApplicationActions applicationActions;
	private JScrollableDesktopPane scrollableDesktopPane;
	private VideoFileManager videoFileManager;
	private DaoService daoService;
	private SettingsManager settingsManager;

	private boolean saveNeeded = false;
	
	/**
	 * This listener will listen to the table panel's event firing
	 */
	private final PropertyChangeListener dirtyListener = new PropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			if (DIRTY.equals(event.getPropertyName())) {
				setSaveNeeded((Boolean) event.getNewValue() || isSaveNeeded());
			}
		}
		
	};

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
		SettingsManager.configureLog4j();
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
		LOGGER.log(Level.INFO, "Starting " + getTitle());
		// register an exception handler on the EDT
		AWTExceptionHandler.registerExceptionHandler();
		ApplicationContext appContext = Application.getInstance().getContext();
		settingsManager = new SettingsManager(appContext.getLocalStorage().getDirectory());
		getSettingsManager().loadSettings();
		// create daoServices and add them to the composite
		DaoService jpaDaoService = new JpaDaoService(getSettingsManager().getDatabaseSettings(), getName());
		DaoService baseEntryDaoService = new BaseEntryDaoService(getSettingsManager().getCalendarSettings(), getVersionString());
		daoService = new CompositeDaoService(jpaDaoService, baseEntryDaoService);
		// create video file manager
		videoFileManager = new VideoFileManager(getSettingsManager());
	}

	/**
	 * Initialize and show the application GUI.
	 */
	protected void startup() {
		// create common application actions class
		applicationActions = new ApplicationActions();
		statusPanel = new StatusPanel();
		clientTablePanel = new ClientTablePanel(getVideoFileManager(), getDaoService());
		addTablePanel(clientTablePanel);
		clientInfoPanel = new ClientInfoPanel(getClientTablePanel(), createUndoableEditListener());
		addTablePanel(clientInfoPanel);
		analysisTablePanel = new AnalysisTablePanel(getClientTablePanel(), createUndoableEditListener(), 
				settingsManager, getVideoFileManager(), getDaoService());
		addTablePanel(analysisTablePanel);
		analysisOverviewTablePanel = new AnalysisOverviewTablePanel(getSettingsManager(), getVideoFileManager());
		addTablePanel(analysisOverviewTablePanel);
		redcordTablePanel = new RedcordTablePanel(getClientTablePanel(), createUndoableEditListener(), getDaoService());
		addTablePanel(redcordTablePanel);
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
		mediaControls = new MediaControls(getSettingsManager(), getVideoFileManager(), 
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
				executeAction(getApplicationActionMap(), SAVE_ACTION);
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
	
	public String getTitle() {
		return getResourceString(APP_TITLE);
	}
	
	public String getName() {
		return getResourceString(APP_NAME);
	}
	
	public String getVersionString() {
		return getResourceString(APP_NAME) + "-" + getResourceString(APP_VERSION) + "-" + getResourceString(APP_BUILD_DATE);
	}
	
	private String getResourceString(String resourceName) {
		return getContext().getResourceMap().getString(resourceName);
	}
	
	/**
	 * Add the given {@link AbstractTablePanel} to the list of panels so it's dirty state can be tracked.
	 * Setting a panel's dirty state to <code>true</code> will enable the save action throughout the application.
	 * @param tablePanel The panel to add to the list
	 */
	private void addTablePanel(AbstractPanel panel) {
		panel.addPropertyChangeListener(dirtyListener);
		panels.add(panel);
	}

	/*
	 * Global application actions
	 */

	@org.jdesktop.application.Action
	public void saveSettings() {
		settingsManager.saveSettings();
	}
	
	public boolean isSaveNeeded() {
		return saveNeeded;
	}

	public void setSaveNeeded(boolean saveNeeded) {
		this.firePropertyChange(SAVE_NEEDED, this.saveNeeded, this.saveNeeded = saveNeeded);
	}
	
	@org.jdesktop.application.Action(enabledProperty=SAVE_NEEDED, block = Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> save() {
		
		return new AbstractTask<Boolean, Void>(SAVE_ACTION) {
			
			protected Boolean doInBackground() throws Exception {
				boolean result = true;
				message("startMessage");
				for(AbstractPanel panel : panels) {
					if (panel.isDirty()) {
						result &= panel.save();
						panel.setDirty(false);
					}
				}
				message("endMessage");
				return result;
			}

			@Override
			protected void succeeded(Boolean result) {
				setSaveNeeded(!result);
			}
			
		};
		
	}

	@org.jdesktop.application.Action(block = Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> refresh() {
		return new RefreshTask(getDaoService(), getClientTablePanel(), getAnalysisTablePanel(), 
				getAnalysisOverviewTablePanel(), getRedcordTablePanel());
	}

	@org.jdesktop.application.Action
	public Task<Void, Void> uploadLogFiles() {
		return new UploadLogFilesTask(settingsManager.getLogFile(), settingsManager.getLogFileUploadUrl());
	}

	private Containable createMainView() {
		final JPanel mainPanel = new JPanel();
		ResourceMap resourceMap = getContext().getResourceMap();
		mainPanel.setName(resourceMap.getString("mainView.title"));
		// create the tabpanel
		JTabbedPane tabPanel = new  JTabbedPane();
		tabPanel.setName("detailTabbedPane");
		int minimumWidth = 0;
		for(AbstractPanel panel : panels) {
			String tabTitle = panel.getResourceMap().getString("tabConstraints.tabTitle");
			if (tabTitle != null) {
				tabPanel.addTab(tabTitle, panel);
			}
			int minimumPanelWidth = panel.getPreferredSize().width;
			minimumWidth = minimumWidth < minimumPanelWidth ? minimumPanelWidth : minimumWidth;
		}

		// set layout and add everything to the frame
		mainPanel.setLayout(new MigLayout("fill, nogrid, flowy, insets 10"));
		mainPanel.add(getClientTablePanel(), "growx");
		mainPanel.add(tabPanel, "height :280:, growx");
		mainPanel.add(getStatusPanel(), "height 30!, gapleft push");
		mainPanel.setMinimumSize(new Dimension(minimumWidth, MAIN_PANEL_MIN_HEIGHT));
		return new Containable() {
			
			public Component getComponent() {
				return mainPanel;
			}

			public String getTitle() {
				return getResourceMap().getString("mainView.title");
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
	
	private SettingsManager getSettingsManager() {
		return settingsManager;
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

	public static class EclipseLinkLogger extends org.eclipse.persistence.logging.AbstractSessionLog {

		public void log(org.eclipse.persistence.logging.SessionLogEntry arg0) {
			LOGGER.log(Level.toLevel(arg0.getLevel()), arg0.getMessage());
		}

	}

}
