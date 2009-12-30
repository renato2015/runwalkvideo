package com.runwalk.video;

import java.awt.Toolkit;
import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
import com.runwalk.video.gui.AnalysisOverviewTableModel;
import com.runwalk.video.gui.AnalysisOverviewTablePanel;
import com.runwalk.video.gui.AnalysisTableModel;
import com.runwalk.video.gui.AnalysisTablePanel;
import com.runwalk.video.gui.ClientInfoPanel;
import com.runwalk.video.gui.ClientTableModel;
import com.runwalk.video.gui.ClientTablePanel;
import com.runwalk.video.gui.MainPanel;
import com.runwalk.video.gui.MyInternalFrame;
import com.runwalk.video.gui.StatusPanel;
import com.runwalk.video.gui.VideoMenuBar;
import com.runwalk.video.gui.VideoToolbar;
import com.runwalk.video.gui.actions.ApplicationActions;
import com.runwalk.video.gui.actions.SyncActions;
import com.runwalk.video.gui.actions.TableActions;
import com.runwalk.video.gui.mediaplayer.PlayerEngine;
import com.runwalk.video.gui.mediaplayer.PlayerFrame;
import com.runwalk.video.util.ApplicationSettings;
import com.runwalk.video.util.ApplicationUtil;
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
	private AnalysisTableModel analysisTableModel;
	private  AnalysisOverviewTablePanel overviewPanel;
	private AnalysisOverviewTableModel analysisOverviewTableModel;
	private  ClientInfoPanel clientInfoPanel;
	private  ClientTableModel clientTableModel;
	private PlayerFrame playerGui;
	private MainPanel mainPanel;

	private VideoToolbar toolBar;
	private VideoMenuBar menuBar;

	private ApplicationActions applicationActions;
	private TableActions tableActions;
	private SyncActions syncActions;
	private PlayerEngine playerEngine;

	private MyInternalFrame controls;

	private EntityManagerFactory emFactory = null;

	private MyInternalFrame playerWindow;

	/**
	 * A convenient static getter for the application instance.
	 * @return the instance of RunwalkVideoApp
	 */
	public static RunwalkVideoApp getApplication() {
		return Application.getInstance(RunwalkVideoApp.class);
	}

	/**
	 * 
	 * Main method launching the application.
	 */
	public static void main(String[] args) {
		ApplicationSettings.configureLog4j();
		/*if (System.getProperty( "javawebstart.version" ) == null) {
			StringBuilder dllPathBuilder = new StringBuilder(System.getProperty("user.dir"));
			if (args.length > 0) {
				dllPathBuilder.append(args[0]);
			} else {
				dllPathBuilder.append("\\target\\lib\\");
			}
			dllPathBuilder.append("dsj.dll");
			LOGGER.debug("DSJ Dll path = " + dllPathBuilder.toString());
			DSEnvironment.setDLLPath(dllPathBuilder.toString());
		}¨*/
		DSEnvironment.setDebugLevel(4);
		DSEnvironment.unlockDLL("jeroen.peelaerts@vaph.be", 610280, 1777185, 0);
		launch(RunwalkVideoApp.class, args);
	}

	private void loadUIState() throws IOException {
		getContext().getSessionStorage().putProperty(MyInternalFrame.class, new MyInternalFrame.InternalFrameProperty());
		getContext().getSessionStorage().restore(getMainFrame(), "desktopPane.xml");
		getContext().getSessionStorage().restore(getControls(), "controlPanel.xml");
		getContext().getSessionStorage().restore(getMainPanel(), "mainPanel.xml");
	}

	private void saveUIState() throws IOException {
		getContext().getSessionStorage().save(getMainFrame(), "desktopPane.xml");
		getContext().getSessionStorage().save(getMainPanel(), "mainPanel.xml");
		if (getControls() != null) {
			getContext().getSessionStorage().save(getControls(), "controls.xml");
		}
	}

	/**
	 * At startup create and show the main frame of the application.
	 */
	@Override 
	protected void startup() {
		ApplicationSettings.getInstance().loadSettings();
		String puName = getContext().getResourceMap().getString("Application.persistenceUnitName");
		emFactory = Persistence.createEntityManagerFactory(puName);
		//the actions that the user can undertake?

		applicationActions = new ApplicationActions();
		tableActions = new TableActions();
		playerEngine = new PlayerEngine();
		syncActions = new SyncActions();

		menuBar = new VideoMenuBar();
		//		toolBar = new VideoToolbar();
		statusPanel = new StatusPanel();
		clientTableModel = new ClientTableModel();
		clientTablePanel = new ClientTablePanel(getClientTableModel());
		clientInfoPanel = new  ClientInfoPanel();
		analysisTableModel = new AnalysisTableModel();
		analysisPanel = new  AnalysisTablePanel(getAnalysisTableModel());
		analysisOverviewTableModel = new AnalysisOverviewTableModel();
		setSaveNeeded(false);
		analysisOverviewTableModel.update();
		overviewPanel = new  AnalysisOverviewTablePanel(getAnalysisOverviewTableModel());    	

		//try to scan for camera at startup :)
		//		(getTableActions().new PrepareSynchronizationTask()).execute();

		playerGui = new PlayerFrame(getPlayer());
		mainPanel = new MainPanel();
		playerEngine.addPropertyChangeListener(playerGui);
		playerGui.startCapturer();

		//add all internal frames from here!!!
		getMainFrame().setJMenuBar(getMenuBar());
		controls = new MyInternalFrame("Player controls", true);
		controls.getContentPane().add(getPlayerGUI().getSouthPanel());

		//		playerWindow = new MyInternalFrame("Afspelen", true);

		//		JToolBar bar = new JToolBar();
		//		bar.setOrientation(JToolBar.HORIZONTAL);
		//		bar.setRollover(true);
		//		bar.add(getPlayerGUI().getSouthPanel());
		//		view.setToolBar(bar);
		JScrollableDesktopPane pane = new JScrollableDesktopPane();
		getMainFrame().add(pane);
		//add the window to the WINDOW menu
		pane.add(mainPanel);
		pane.add(controls);
		//		pane.add(playerWindow);

		controls.pack();
		controls.setVisible(true);
		getMenuBar().addWindow(controls);

		mainPanel.pack();
		mainPanel.setVisible(true);
		getMenuBar().addWindow(mainPanel);

		//		playerWindow.pack();
		//		playerWindow.setVisible(true);
		//		getMenuBar().addWindow(playerWindow);

		show(getMainFrame());

		try {
			loadUIState();
		} catch (IOException e) {
			LOGGER.error("Failed to load UI state", e);
		}
	}


	public MyInternalFrame getControls() {
		return controls;
	}


	@Override protected void shutdown() {
		Task<Void, Void> saveTask = null;
		if (isSaveNeeded()) {
			int result = JOptionPane.showConfirmDialog(getMainFrame(), 
					"Wilt u de gemaakte wijzigingen opslaan?", 
					"Wijzingen bewaren..", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				saveTask = getApplicationActions().save();
				saveTask.execute();
			}
		}
		Task<Void, Void> uploadTask = getApplicationActions().uploadLogFiles();
		uploadTask.execute();
		ApplicationSettings.getInstance().saveSettings();
		try {
			if (getPlayer() != null) {
				ApplicationUtil.disposeDSGraph(getPlayer().getCaptureGraph());
				ApplicationUtil.disposeDSGraph(getPlayer().getDSMovieGraph());
			}
			saveUIState();
			emFactory.close();
		} catch (IOException e) {
			LOGGER.error("Failed to save UI state", e);
			System.exit(1);
		}
		while (!uploadTask.isDone() || saveTask != null && !saveTask.isDone()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				LOGGER.error(e);
			}
		}
	}

	//Getters & Setters for the main objects in this program

	public ApplicationActions getApplicationActions() {
		return applicationActions;
	}

	public TableActions getTableActions() {
		return tableActions;
	}

	public SyncActions getSyncActions() {
		return syncActions;
	}

	public PlayerEngine getPlayer() {
		return playerEngine;
	}

	public AnalysisTableModel getAnalysisTableModel() {
		return analysisTableModel;
	}

	public VideoMenuBar getMenuBar() {
		return menuBar;
	}

	public VideoToolbar getToolBar() {
		return toolBar;
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

	public ClientTableModel getClientTableModel() {
		return clientTableModel;
	}

	public AnalysisOverviewTableModel getAnalysisOverviewTableModel() {
		return analysisOverviewTableModel;
	}

	public PlayerEngine getPlayerEngine() {
		return playerEngine;
	}

	public PlayerFrame getPlayerGUI() {
		return playerGui;
	}

	public MainPanel getMainPanel() {
		return mainPanel;
	}

	//some convenience methods

	public boolean isSaveNeeded() {
		return getApplicationActions().isSaveNeeded();
	}

	public void setSaveNeeded(boolean saveNeeded) {
		getApplicationActions().setSaveNeeded(saveNeeded);
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return emFactory;
	}

	public Query createQuery(String query) {
		return getEntityManagerFactory().createEntityManager().createQuery(query).setHint("toplink.refresh", "true").setHint("oracle.toplink.essentials.config.CascadePolicy", "CascadePrivateParts ");
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
		return getClientTableModel().getSelectedItem();
	}

	public Analysis getSelectedAnalysis() {
		return getAnalysisTableModel().getSelectedItem();
	}

	public void clearStatusMessage() {
		getStatusPanel().showMessage("");
	}

	//getters for action maps in this application
	public ActionMap getActionMap(Object obj) {
		return getContext().getActionMap(obj);
	}

	public ActionMap getTableActionMap() {
		return getActionMap(getTableActions());
	}

	public ActionMap getApplicationActionMap() {
		return getActionMap(getApplicationActions());
	}

	public ActionMap getPlayerActionMap() {
		return getActionMap(getPlayer());
	}

	public static class RunwalkLogger extends org.eclipse.persistence.logging.AbstractSessionLog {

		@Override
		public void log(org.eclipse.persistence.logging.SessionLogEntry arg0) {
			LOGGER.log(Level.toLevel(arg0.getLevel()), arg0.getMessage());
		}

	}

	public MyInternalFrame getPlayerWindow() {
		return playerWindow;
	}

}
