package com.runwalk.video.panels;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.TaskMonitor;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.io.DateVideoFolderRetrievalStrategy;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.io.VideoFolderRetrievalStrategy;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.tasks.CheckFreeDiskSpaceTask;
import com.runwalk.video.tasks.CleanupVideoFilesTask;
import com.runwalk.video.tasks.OrganiseVideoFilesTask;
import com.runwalk.video.tasks.RefreshTask;
import com.runwalk.video.tasks.RefreshVideoFilesTask;
import com.runwalk.video.ui.actions.ApplicationActionConstants;

@SuppressWarnings("serial")
public class AnalysisOverviewTablePanel extends AbstractTablePanel<Analysis> implements PropertyChangeListener {

	public static final String CLEANUP_VIDEO_FILES_ACTION = "cleanupVideoFiles";
	public static final String SELECT_VIDEO_DIR_ACTION = "selectVideoDir";
	public static final String SELECT_UNCOMPRESSED_VIDEO_DIR_ACTION = "selectUncompressedVideoDir";

	final ImageIcon completedIcon = getResourceMap().getImageIcon("status.complete.icon");
	final ImageIcon incompleteIcon = getResourceMap().getImageIcon("status.incomplete.icon");

	private final VideoFileManager videoFileManager;
	private final SettingsManager appSettings;

	public AnalysisOverviewTablePanel(SettingsManager appSettings, VideoFileManager videoFileManager) {
		super(new MigLayout("fill, nogrid"));
		this.videoFileManager = videoFileManager;
		this.appSettings = appSettings;

		/*JScrollPane scrollPane = new  JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow");*/

		JButton cleanupButton = new JButton(getAction(CLEANUP_VIDEO_FILES_ACTION));
		cleanupButton.setFont(SettingsManager.MAIN_FONT);
		add(cleanupButton);
		// add a listener to start tasks upon finishing the refresh task
		getTaskMonitor().addPropertyChangeListener(this);
	}

	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean, Void> refreshVideoFiles() {
		/*RefreshVideoFilesTask refreshVideoFilesTask = new RefreshVideoFilesTask(getVideoFileManager(), getAnalysisList());
		refreshVideoFilesTask.addTaskListener(new TaskListener.Adapter<Boolean, Void>() {

			@Override
			public void succeeded(TaskEvent<Boolean> event) {
				//setCompressionEnabled(event.getValue());
			}

		});
		return refreshVideoFilesTask;*/
		return null;
	}

	@Action
	public Task<Long, Void> checkFreeDiskSpace() {
		Window parentWindow = SwingUtilities.windowForComponent(this);
		return new CheckFreeDiskSpaceTask(parentWindow, getVideoFileManager());
	}

	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean, Void> cleanupVideoFiles() {
		Window parentWindow = SwingUtilities.windowForComponent(this);
		return new CleanupVideoFilesTask(parentWindow, getVideoFileManager());
	}

	@Action(block = Task.BlockingScope.APPLICATION)
	public Task<Void, Void> organiseVideoFiles() {
		Object formatString = JOptionPane.showInputDialog(
				SwingUtilities.windowForComponent(this), 
				getResourceMap().getString("organiseVideoFiles.confirmDialog.text"), 
				getResourceMap().getString("organiseVideoFiles.Action.text"), JOptionPane.QUESTION_MESSAGE, 
				null, null, getVideoFileManager().getVideoFolderRetrievalStrategy().getDisplayString());
		if (formatString != null) {
			// create a new retrieval strategy using the specified format string
			VideoFolderRetrievalStrategy newStrategy = new DateVideoFolderRetrievalStrategy(formatString.toString());
			Window parentComponent = SwingUtilities.windowForComponent(this);
			return new OrganiseVideoFilesTask(parentComponent, getVideoFileManager(), newStrategy);
		}
		return null;
	}

	private File selectDirectory(File chosenDir, String title) {
		final JFileChooser chooser = chosenDir == null ? new JFileChooser() : new JFileChooser(chosenDir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showDialog(null, title);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return chosenDir;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public SettingsManager getAppSettings() {
		return appSettings;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		Object oldValue = evt.getOldValue();
		if (TaskMonitor.PROP_FOREGROUND_TASK.equals(evt.getPropertyName()) && oldValue != null) {
			Window parentComponent = SwingUtilities.windowForComponent(this);
			Class<?> taskClass = oldValue.getClass();
			if (taskClass == RefreshTask.class) {
				invokeAction(ApplicationActionConstants.REFRESH_VIDEO_FILES_ACTION, parentComponent);
			} else if (taskClass == RefreshVideoFilesTask.class) {
				invokeAction(ApplicationActionConstants.CHECK_FREE_DISK_SPACE_ACTION, parentComponent);
			}
		}
	}

	@Override
	void initialiseTableColumnModel() {
		// TODO Auto-generated method stub
		
	}

}
