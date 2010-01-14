package com.runwalk.video;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.List;

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
import com.runwalk.video.gui.AnalysisOverviewTablePanel;
import com.runwalk.video.gui.AnalysisTablePanel;
import com.runwalk.video.gui.ClientInfoPanel;
import com.runwalk.video.gui.ClientTablePanel;
import com.runwalk.video.gui.MainInternalFrame;
import com.runwalk.video.gui.MyInternalFrame;
import com.runwalk.video.gui.StatusPanel;
import com.runwalk.video.gui.VideoMenuBar;
import com.runwalk.video.gui.actions.ApplicationActions;
import com.runwalk.video.gui.actions.SyncActions;
import com.runwalk.video.gui.media.PlayerInternalFrame;
import com.runwalk.video.util.ApplicationSettings;
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
	private PlayerInternalFrame playerInternalFrame;
	private MainInternalFrame mainInternalFrame;

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
		getContext().getSessionStorage().restore(getPlayerInternalFrame(), "controlFrame.xml");
		getContext().getSessionStorage().restore(getMainInternalFrame(), "mainFrame.xml");
	}

	private void saveUIState() throws IOException {
		getContext().getSessionStorage().save(getMainFrame(), "desktopPane.xml");
		getContext().getSessionStorage().save(getMainInternalFrame(), "mainFrame.xml");
		if (getPlayerInternalFrame() != null) {
			getContext().getSessionStorage().save(getPlayerInternalFrame(), "controlFrame.xml");
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

		statusPanel = new StatusPanel();
		clientTablePanel = new ClientTablePanel();
		clientInfoPanel = new  ClientInfoPanel();
		analysisPanel = new  AnalysisTablePanel();
		setSaveNeeded(false);
		overviewPanel = new  AnalysisOverviewTablePanel();    	

		menuBar = new VideoMenuBar();

		playerInternalFrame = new PlayerInternalFrame();
		playerInternalFrame.startCapturer();
		mainInternalFrame = new MainInternalFrame();
		
		//add all internal frames from here!!!
		getMainFrame().setJMenuBar(getMenuBar().getComponent());

		pane = new JScrollableDesktopPane();
		getMainFrame().add(pane);
		
		//add the window to the WINDOW menu
		addInternalFrame(getPlayerInternalFrame());
		addInternalFrame(getMainInternalFrame());

		show(getMainFrame());

		try {
			loadUIState();
		} catch (IOException e) {
			LOGGER.error("Failed to load UI state", e);
		}
	}
	
	public void addInternalFrame(BaseInternalFrame frame) {
		frame.pack();
		frame.setVisible(true);
		getMenuBar().addWindow(frame);
		pane.add(frame);
	}

	@Override 
	protected void shutdown() {
		Task<List<Client>, Void> saveTask = null;
		if (isSaveNeeded()) {
			int result = JOptionPane.showConfirmDialog(getMainFrame(), 
					"Wilt u de gemaakte wijzigingen opslaan?", 
					"Wijzingen bewaren..", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				saveTask = getClientTablePanel().save();
				saveTask.execute();
			}
		}
		Task<Void, Void> uploadTask = getApplicationActions().uploadLogFiles();
		uploadTask.execute();
		ApplicationSettings.getInstance().saveSettings();
		try {
			if (getPlayerPanel() != null) {
				
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

	public BaseInternalFrame getPlayerInternalFrame() {
		return playerInternalFrame.getComponent();
	}
	
	public BaseInternalFrame getMainInternalFrame() {
		return mainInternalFrame.getComponent();
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

	public PlayerInternalFrame getPlayerPanel() {
		return playerInternalFrame;
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

		@Override
		public void log(org.eclipse.persistence.logging.SessionLogEntry arg0) {
			LOGGER.log(Level.toLevel(arg0.getLevel()), arg0.getMessage());
		}

	}

}
