package com.runwalk.video.gui.tasks;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import ca.odell.glazedlists.EventList;

import com.google.common.collect.Lists;
import com.runwalk.video.VideoFileManager;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;

public class CleanupVideoFilesTask extends AbstractTask<Boolean, Void> {

	private int filesDeleted = 0, fileCount = 0;

	private final VideoFileManager videoFileManager;
	
	private final EventList<Analysis> analysisList;
	
	private final Component parentComponent;

	public CleanupVideoFilesTask(Component parentComponent, EventList<Analysis> analysisList, VideoFileManager videoFileManager) {
		super("cleanupVideoFiles");
		this.parentComponent = parentComponent;
		this.videoFileManager = videoFileManager;
		this.analysisList = analysisList;
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		message("startMessage");
		List<File> filesToDelete = Lists.newArrayList();
		getAnalysisList().getReadWriteLock().readLock().lock();
		try {
			int progress = 0;
			for(Analysis analysis : getAnalysisList()) {
				for(Recording recording : analysis.getRecordings()) {
					File compressedVideoFile = getVideoFileManager().getCompressedVideoFile(recording);
					File uncompressedVideoFile = getVideoFileManager().getUncompressedVideoFile(recording);
					if (compressedVideoFile.exists() && uncompressedVideoFile.exists()) {
						long compressedDuration = VideoFileManager.getDuration(compressedVideoFile);
						long uncompressedDuration = VideoFileManager.getDuration(uncompressedVideoFile);
						if (compressedDuration == uncompressedDuration) {
							filesToDelete.add(uncompressedVideoFile);
						}
					}
				}
				setProgress(++progress, 0, getAnalysisList().size());
			}
		} finally {
			getAnalysisList().getReadWriteLock().readLock().unlock();
		}
		fileCount = filesToDelete.size();
		filesDeleted = deleteFiles(filesToDelete);
		message("endMessage");
		return fileCount == filesDeleted;
	}
	
	private int deleteFiles(List<File> filesToDelete) {
		int filesDeleted = 0;
		if (!filesToDelete.isEmpty()) {
			int chosenOption = JOptionPane.showConfirmDialog(getParentComponent(), 
					getResourceString("filesFoundMessage", filesToDelete.size()), 
					getResourceString("startMessage"), JOptionPane.OK_CANCEL_OPTION);
			if (chosenOption == JOptionPane.OK_OPTION) {
				for (File file : filesToDelete) {
					if (file.delete()) {
						filesDeleted++;
					} 
				}
			}
		}
		return filesDeleted;
	}

	@Override
	protected void succeeded(Boolean result) {
		try {
			String dialogMsg = getResourceString("finishedMessage", filesDeleted); 
			String dialogTitle = getResourceString("endMessage");
			if (fileCount == 0) {
				JOptionPane.showMessageDialog(getParentComponent(), 
						getResourceString("noFilesFoundMessage"), 
						dialogTitle, JOptionPane.INFORMATION_MESSAGE);
			} else if (get()) {
				JOptionPane.showMessageDialog(getParentComponent(),
						dialogMsg, dialogTitle, JOptionPane.PLAIN_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(getParentComponent(),
						dialogMsg + getResourceString("endErrorMessage", fileCount - filesDeleted),
						dialogTitle, JOptionPane.WARNING_MESSAGE); 
			}
		} catch (Exception e) {
			getLogger().error(e);
		} 
	}
	
	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public EventList<Analysis> getAnalysisList() {
		return analysisList;
	}

	public Component getParentComponent() {
		return parentComponent;
	}
	
}