package com.runwalk.video.tasks;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.common.collect.Lists;
import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.io.VideoFileManager;

public class CleanupVideoFilesTask extends AbstractTask<Boolean, Void> {

	private int filesDeleted = 0, fileCount = 0;

	private final VideoFileManager videoFileManager;

	private final Component parentComponent;

	private final List<Recording> recordings;

	public CleanupVideoFilesTask(Component parentComponent, VideoFileManager videoFileManager, List<Recording> recordings) {
		super("cleanupVideoFiles");
		this.parentComponent = parentComponent;
		this.videoFileManager = videoFileManager;
		this.recordings = recordings;
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		message("startMessage");
		List<File> filesToDelete = Lists.newArrayList();
		int progress = 0;
		// should get all video files in db??
		for(Recording recording : recordings) {
			File compressedVideoFile = getVideoFileManager().getCompressedVideoFile(recording);
			File uncompressedVideoFile = getVideoFileManager().getUncompressedVideoFile(recording);
			if (compressedVideoFile.exists() && uncompressedVideoFile.exists()) {
				long compressedDuration = VideoFileManager.getDuration(compressedVideoFile);
				long uncompressedDuration = VideoFileManager.getDuration(uncompressedVideoFile);
				if (compressedDuration == uncompressedDuration) {
					filesToDelete.add(uncompressedVideoFile);
				}
			}
			setProgress(++progress, 0, recordings.size());
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
			} else {
				filesDeleted = -1;
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
			} else if (filesDeleted != -1) {
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
	
	public List<Recording> getRecordings() {
		return recordings;
	}

	public Component getParentComponent() {
		return parentComponent;
	}

}