package com.runwalk.video.gui.panels;

import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.matchers.Matcher;

import com.runwalk.video.DateVideoFolderRetrievalStrategy;
import com.runwalk.video.VideoFileManager;
import com.runwalk.video.VideoFolderRetrievalStrategy;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.gui.DateTableCellRenderer;
import com.runwalk.video.gui.tasks.CleanupVideoFilesTask;
import com.runwalk.video.gui.tasks.CompressVideoFilesTask;
import com.runwalk.video.gui.tasks.OrganiseVideoFilesTask;
import com.runwalk.video.gui.tasks.RefreshVideoFilesTask;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class AnalysisOverviewTablePanel extends AbstractTablePanel<Analysis> {

	private static final String COMPRESSION_ENABLED = "compressionEnabled";

	private boolean compressionEnabled;
	final ImageIcon completedIcon = getResourceMap().getImageIcon("status.complete.icon");
	final ImageIcon incompleteIcon = getResourceMap().getImageIcon("status.incomplete.icon");

	private EventList<Analysis> analysisList = GlazedLists.eventListOf();

	private final VideoFileManager videoFileManager;
	private final AppSettings appSettings;

	public AnalysisOverviewTablePanel(AppSettings appSettings, VideoFileManager videoFileManager) {
		super(new MigLayout("fill, nogrid"));
		this.videoFileManager = videoFileManager;
		this.appSettings = appSettings;

		JScrollPane scrollPane = new  JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow");

		JButton cleanupButton = new JButton(getAction("cleanupVideoFiles"));
		cleanupButton.setFont(AppSettings.MAIN_FONT);
		add(cleanupButton);
		setSecondButton(new JButton(getAction("compressVideoFiles")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		add(getSecondButton());
	}
	
	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean, Void> refreshVideoFiles() {
		RefreshVideoFilesTask refreshVideoFilesTask = new RefreshVideoFilesTask(getVideoFileManager(), getAnalysisList());
		refreshVideoFilesTask.addTaskListener(new TaskListener.Adapter<Boolean, Void>() {

			@Override
			public void succeeded(TaskEvent<Boolean> event) {
				setCompressionEnabled(event.getValue());
			}
			
		});
		return refreshVideoFilesTask;
	}

	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean, Void> cleanupVideoFiles() {
		Window parentWindow = SwingUtilities.windowForComponent(this);
		return new CleanupVideoFilesTask(parentWindow, getAnalysisList(), getVideoFileManager());
	}
	
	@Action(block = Task.BlockingScope.APPLICATION)
	public Task<Void, Void> organiseVideoFiles() {
		Object formatString = JOptionPane.showInputDialog(SwingUtilities.windowForComponent(this), 
				"Geef hier een folder structuur op door '/' als separator te gebruiken", 
				"Organiseer bestanden", JOptionPane.QUESTION_MESSAGE, 
				null, null, getVideoFileManager().getVideoFolderRetrievalStrategy().getDisplayString());
		if (formatString != null) {
			// create a new retrieval strategy using the specified format string
			VideoFolderRetrievalStrategy newStrategy = new DateVideoFolderRetrievalStrategy(formatString.toString());
			Window parentComponent = SwingUtilities.windowForComponent(this);
			return new OrganiseVideoFilesTask(parentComponent, getAnalysisList(), getVideoFileManager(), newStrategy);
		}
		return null;
	}

	@Action(enabledProperty = COMPRESSION_ENABLED, block = Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> compressVideoFiles() {
		setCompressionEnabled(false);
		String transcoder = getAppSettings().getTranscoderName();
		return new CompressVideoFilesTask(getVideoFileManager(), getCompressableRecordings(), transcoder);
	}

	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean, Void> selectUncompressedVideoDir() {
		File chosenDir = getAppSettings().getUncompressedVideoDir();
		File result = selectDirectory(chosenDir);
		getAppSettings().setUncompressedVideoDir(result);
		return refreshVideoFiles();
	}

	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean,Void> selectVideoDir() {
		File chosenDir = getAppSettings().getVideoDir();
		File result = selectDirectory(chosenDir);
		getAppSettings().setVideoDir(result);
		return refreshVideoFiles();
	}
	
	private File selectDirectory(File chosenDir) {
		final JFileChooser chooser = chosenDir == null ? new JFileChooser() : new JFileChooser(chosenDir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showDialog(null, "Selecteer");
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return chosenDir;
	}

	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(boolean compressionEnabled) {
		firePropertyChange(COMPRESSION_ENABLED, this.compressionEnabled, this.compressionEnabled = compressionEnabled);
	}

	/**
	 * Returns a {@link java.awt.List} of }link Recording}s that are in the {@link RecordingStatus#UNCOMPRESSED} state.
	 * 
	 * @return The list
	 */
	private List<Recording> getCompressableRecordings() {
		List<Recording> list = new ArrayList<Recording>();
		for (Analysis analysis : getItemList()) {
			for (Recording recording : analysis.getRecordings()) {
				if (recording.isUncompressed() && getVideoFileManager().canReadAndExists(recording)) {
					list.add(recording);
				}
			}
		}
		return list;
	}

	private EventList<Analysis> getAnalysisList() { 
		return analysisList;
	}
	
	private void setAnalysisList(EventList<Analysis> analysisList) {
		this.analysisList = analysisList;
	}

	@Override
	protected EventList<Analysis> specializeItemList(EventList<Analysis> eventList) {
		setAnalysisList(eventList);
		return new FilterList<Analysis>(eventList, new Matcher<Analysis>() {

			public boolean matches(Analysis item) {
				for (Recording recording : item.getRecordings()) {
					if (recording.isCompressable()) {
						return true;
					}
				}
				return false;
			}

		});
	}

	@Override
	public void setItemList(EventList<Analysis> itemList, ObservableElementList.Connector<Analysis> itemConnector) {
		super.setItemList(itemList, itemConnector);
		getTable().getColumnModel().getColumn(0).setCellRenderer(new CustomJTableRenderer(getTable().getDefaultRenderer(ImageIcon.class)));
		getTable().getColumnModel().getColumn(0).setMaxWidth(25);
		getTable().getColumnModel().getColumn(1).setCellRenderer(new DateTableCellRenderer(AppUtil.EXTENDED_DATE_FORMATTER));
		getTable().getColumnModel().getColumn(1).setPreferredWidth(80);
		getTable().getColumnModel().getColumn(4).setCellRenderer(new DateTableCellRenderer(AppUtil.DURATION_FORMATTER));
		getTable().getColumnModel().getColumn(4).setPreferredWidth(60);
		getTable().getColumnModel().getColumn(5).setPreferredWidth(60);
		getTable().getColumnModel().getColumn(6).setCellRenderer(new CustomJTableRenderer(getTable().getDefaultRenderer(JButton.class)));
		getTable().getColumnModel().getColumn(6).setPreferredWidth(40);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		addMouseListenerToTable();
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public AppSettings getAppSettings() {
		return appSettings;
	}

}
