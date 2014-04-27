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
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.Matcher;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.jpa.RecordingDao;
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
import com.runwalk.video.tasks.SaveTask;
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
	public static final String REFRESH_RECORDINGS_ACTION = "refreshRecordings";

	private boolean compressionEnabled;
	final ImageIcon completedIcon = getResourceMap().getImageIcon("status.complete.icon");
	final ImageIcon incompleteIcon = getResourceMap().getImageIcon("status.incomplete.icon");

	private final VideoFileManager videoFileManager;
	private final SettingsManager appSettings;
	private final DaoService daoService;
	
	private EventList<Recording> recordingList = GlazedLists.eventListOf();

	public RecordingTablePanel(SettingsManager appSettings, VideoFileManager videoFileManager, DaoService daoService) {
		super(new MigLayout("fill, nogrid"));
		this.videoFileManager = videoFileManager;
		this.appSettings = appSettings;
		this.daoService = daoService;

		JScrollPane scrollPane = new  JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow");

		JButton cleanupButton = new JButton(getAction(CLEANUP_VIDEO_FILES_ACTION));
		cleanupButton.setFont(SettingsManager.MAIN_FONT);
		add(cleanupButton);
		
		JButton compressButton = new JButton(getAction(COMPRESS_VIDEO_FILES_ACTION));
		compressButton.setFont(SettingsManager.MAIN_FONT);
		add(compressButton);
		
		JButton refreshButton = new JButton(getAction(REFRESH_RECORDINGS_ACTION));
		refreshButton.setFont(SettingsManager.MAIN_FONT);
		add(refreshButton);
	}

	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean, Void> refreshVideoFiles() {
		RefreshVideoFilesTask refreshVideoFilesTask = new RefreshVideoFilesTask(getVideoFileManager(), recordingList);
		refreshVideoFilesTask.addTaskListener(new TaskListener.Adapter<Boolean, Void>() {

			@Override
			public void succeeded(TaskEvent<Boolean> event) {
				setCompressionEnabled(event.getValue());
			}

		});
		return refreshVideoFilesTask;
	}
	
	@Action(block = BlockingScope.APPLICATION)
	public AbstractTask<EventList<RecordingModel>, Void> refreshRecordings() {
		dispose();
		return new AbstractTask<EventList<RecordingModel>, Void>(REFRESH_RECORDINGS_ACTION) {

			@Override
			protected EventList<RecordingModel> doInBackground() throws Exception {
				message("startMessage");
				RecordingDao recordingDao = getDaoService().getDao(Recording.class);
				List<RecordingModel> recordingModelList = recordingDao
						.getRecordingsAsModelByStatusCode(RecordingStatus.UNCOMPRESSED.getCode());
				EventList<RecordingModel> recordingModelEventList = GlazedLists.eventList(recordingModelList);
				recordingList = transformRecordingModelList(recordingModelEventList);
				message("endMessage");
				return recordingModelEventList;
			}

			@Override
			protected void succeeded(EventList<RecordingModel> recordingModels) {
				setItemList(recordingModels);
				setCompressionEnabled(recordingModels.size() > 0);
			}
			
		};
	}
	
	/**
	 * Returns a {@link java.awt.List} of {@link Recording}s that are in the {@link RecordingStatus#UNCOMPRESSED} state.
	 * 
	 * @return The list
	 */
	private EventList<Recording> transformRecordingModelList(EventList<RecordingModel> sourceList) {
		sourceList.getReadWriteLock().readLock().lock();
		try {
			return new FunctionList<RecordingModel, Recording>(sourceList, new FunctionList.Function<RecordingModel, Recording>() {

				@Override
				public Recording evaluate(RecordingModel sourceValue) {
					return sourceValue.getEntity();
				}
					
			});
		} finally {
			sourceList.getReadWriteLock().readLock().unlock();
		}
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
	public Task<Boolean, ?> compressVideoFiles() {
		setCompressionEnabled(false);
		String transcoder = getAppSettings().getTranscoderName();
		Window parentComponent = SwingUtilities.windowForComponent(this);
		return new CompressVideoFilesTask(parentComponent, getVideoFileManager(), recordingList, transcoder) {

			@Override
			protected void process(List<Recording> recordings) {
				try {
					getItemList().getReadWriteLock().readLock().lock();
					for (Recording recording : recordings) {
						for(RecordingModel recordingModel : getItemList()) {
							if (recording.getId().equals(recordingModel.getId())) {
								recordingModel.setDirty(true);
							}
						}
					}
				} finally {
					getItemList().getReadWriteLock().readLock().unlock();
				}
				
			}

			@Override
			protected void succeeded(Boolean result) {
				setDirty(true);
			}
			
		};
	}
	
	@Override
	public boolean save() {
		try {
			getRecordingList().getReadWriteLock().readLock().lock();
			EventList<Recording> transformRecordingModelList = transformRecordingModelList(new FilterList<RecordingModel>(getItemList(), new Matcher<RecordingModel>() {

				public boolean matches(RecordingModel item) {
					return item.isDirty();
				}
					
			}));
			getTaskService().execute(new SaveTask<Recording>(getDaoService(), Recording.class, transformRecordingModelList));
		} finally {
			getRecordingList().getReadWriteLock().readLock().unlock();
		}
		return true;
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

	public void initialiseTableColumnModel() {
		// previously an icon was rendered in the first column, this is not the case any more
		String defaultValue = getResourceMap().getString("tableFormat.defaultValue");
		getTable().getColumnModel().getColumn(0).setCellRenderer(new DateTableCellRenderer(defaultValue, AppUtil.EXTENDED_DATE_FORMATTER));
		getTable().getColumnModel().getColumn(0).setPreferredWidth(80);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		getTable().getColumnModel().getColumn(3).setCellRenderer(new DateTableCellRenderer(defaultValue, AppUtil.DURATION_FORMATTER));
		getTable().getColumnModel().getColumn(3).setPreferredWidth(60);
		getTable().getColumnModel().getColumn(4).setPreferredWidth(60);
		final String buttonTitle = getResourceMap().getString("recordingModelTableFormat.openButton.text");
		getTable().getColumnModel().getColumn(5).setCellRenderer(new JButtonTableCellRenderer(buttonTitle));
		getTable().getColumnModel().getColumn(5).setPreferredWidth(40);
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public SettingsManager getAppSettings() {
		return appSettings;
	}

	public DaoService getDaoService() {
		return daoService;
	}

	public EventList<Recording> getRecordingList() {
		return recordingList;
	}
	
}
