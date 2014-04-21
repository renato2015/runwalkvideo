package com.runwalk.video.panels;

import java.awt.Window;
import java.io.File;
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
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;

import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.io.DateVideoFolderRetrievalStrategy;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.io.VideoFolderRetrievalStrategy;
import com.runwalk.video.model.RecordingModel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.tasks.AbstractTask;
import com.runwalk.video.tasks.CheckFreeDiskSpaceTask;
import com.runwalk.video.tasks.CleanupVideoFilesTask;
import com.runwalk.video.tasks.CompressVideoFilesTask;
import com.runwalk.video.tasks.OrganiseVideoFilesTask;
import com.runwalk.video.tasks.RefreshVideoFilesTask;
import com.runwalk.video.ui.table.DateTableCellRenderer;
import com.runwalk.video.ui.table.JButtonTableCellRenderer;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class RecordingTablePanel extends AbstractTablePanel<RecordingModel> {

	private static final String COMPRESSION_ENABLED = "compressionEnabled";

	public static final String COMPRESS_VIDEO_FILES_ACTION = "compressVideoFiles";
	public static final String CLEANUP_VIDEO_FILES_ACTION = "cleanupVideoFiles";
	public static final String SELECT_VIDEO_DIR_ACTION = "selectVideoDir";
	public static final String SELECT_UNCOMPRESSED_VIDEO_DIR_ACTION = "selectUncompressedVideoDir";

	private boolean compressionEnabled;
	final ImageIcon completedIcon = getResourceMap().getImageIcon("status.complete.icon");
	final ImageIcon incompleteIcon = getResourceMap().getImageIcon("status.incomplete.icon");

	private final VideoFileManager videoFileManager;
	private final SettingsManager appSettings;

	public RecordingTablePanel(SettingsManager appSettings, VideoFileManager videoFileManager) {
		super(new MigLayout("fill, nogrid"));
		this.videoFileManager = videoFileManager;
		this.appSettings = appSettings;

		JScrollPane scrollPane = new  JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow");

		JButton cleanupButton = new JButton(getAction(CLEANUP_VIDEO_FILES_ACTION));
		cleanupButton.setFont(SettingsManager.MAIN_FONT);
		add(cleanupButton);
		setSecondButton(new JButton(getAction(COMPRESS_VIDEO_FILES_ACTION)));
		getSecondButton().setFont(SettingsManager.MAIN_FONT);
		add(getSecondButton());
	}

	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean, Void> refreshVideoFiles() {
		RefreshVideoFilesTask refreshVideoFilesTask = new RefreshVideoFilesTask(getVideoFileManager(), transformRecordingModelList());
		refreshVideoFilesTask.addTaskListener(new TaskListener.Adapter<Boolean, Void>() {

			@Override
			public void succeeded(TaskEvent<Boolean> event) {
				setCompressionEnabled(event.getValue());
			}

		});
		return refreshVideoFilesTask;
	}
	
	public AbstractTask<List<Recording>, Void> refreshRecordings() {
		return new AbstractTask<List<Recording>, Void>("refreshRecordings") {

			@Override
			protected List<Recording> doInBackground() throws Exception {
				//return getDaoService().getDao(Recording.class);
				return null;
			}
			
		};
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

	@Action(enabledProperty = COMPRESSION_ENABLED, block = Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> compressVideoFiles() {
		setCompressionEnabled(false);
		String transcoder = getAppSettings().getTranscoderName();
		Window parentComponent = SwingUtilities.windowForComponent(this);
		return new CompressVideoFilesTask(parentComponent, getVideoFileManager(), transformRecordingModelList(), transcoder);
	}

	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean, Void> selectUncompressedVideoDir() {
		Task<Boolean, Void> result = null;
		File oldDir = getAppSettings().getUncompressedVideoDir();
		javax.swing.Action action = getAction(SELECT_UNCOMPRESSED_VIDEO_DIR_ACTION);
		String title = action.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString();
		File newDir = selectDirectory(oldDir, title);
		if (!newDir.equals(oldDir)) {
			getAppSettings().setUncompressedVideoDir(newDir);
			result = refreshVideoFiles();
		}
		return result;
	}

	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean,Void> selectVideoDir() {
		Task<Boolean, Void> result = null;
		File oldDir = getAppSettings().getVideoDir();
		javax.swing.Action action = getAction(SELECT_VIDEO_DIR_ACTION);
		String title = action.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString();
		File newDir = selectDirectory(oldDir, title);
		if (!newDir.equals(oldDir)) {
			getAppSettings().setVideoDir(newDir);
			result = refreshVideoFiles();
		}
		return result;
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

	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(boolean compressionEnabled) {
		// compression only works on windows atm (dsj)
		boolean isWindows = AppHelper.getPlatform() == PlatformType.WINDOWS;
		firePropertyChange(COMPRESSION_ENABLED, this.compressionEnabled, this.compressionEnabled = compressionEnabled && isWindows);
	}

	/**
	 * Returns a {@link java.awt.List} of {@link Recording}s that are in the {@link RecordingStatus#UNCOMPRESSED} state.
	 * 
	 * @return The list
	 */
	private EventList<Recording> transformRecordingModelList() {
		getItemList().getReadWriteLock().readLock().lock();
		try {
			return new FunctionList<RecordingModel, Recording>(getItemList(), new FunctionList.Function<RecordingModel, Recording>() {

				@Override
				public Recording evaluate(RecordingModel sourceValue) {
					return sourceValue.getEntity();
				}
					
			});
		} finally {
			getItemList().getReadWriteLock().readLock().unlock();
		}
	}

	public void initialiseTableColumnModel() {
		// previously an icon was rendered in the first column, this is not the case any more
		getTable().getColumnModel().getColumn(0).setMaxWidth(25);
		String defaultValue = getResourceMap().getString("tableFormat.defaultValue");
		getTable().getColumnModel().getColumn(1).setCellRenderer(new DateTableCellRenderer(defaultValue, AppUtil.EXTENDED_DATE_FORMATTER));
		getTable().getColumnModel().getColumn(1).setPreferredWidth(80);
		getTable().getColumnModel().getColumn(4).setCellRenderer(new DateTableCellRenderer(defaultValue, AppUtil.DURATION_FORMATTER));
		getTable().getColumnModel().getColumn(4).setPreferredWidth(60);
		getTable().getColumnModel().getColumn(5).setPreferredWidth(60);
		final String buttonTitle = getResourceMap().getString("analysisOverviewTableFormat.openButton.text");
		getTable().getColumnModel().getColumn(6).setCellRenderer(new JButtonTableCellRenderer(buttonTitle));
		getTable().getColumnModel().getColumn(6).setPreferredWidth(40);
		getTable().getColumnModel().getColumn(0).setResizable(false);
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public SettingsManager getAppSettings() {
		return appSettings;
	}

}
