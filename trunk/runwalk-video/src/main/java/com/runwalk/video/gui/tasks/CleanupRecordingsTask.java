package com.runwalk.video.gui.tasks;

import java.awt.Frame;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.common.collect.Lists;
import com.runwalk.video.VideoFileManager;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;

public class CleanupRecordingsTask extends AbstractTask<Boolean, Void> {

	private int filesDeleted = 0, fileCount = 0;

	private final VideoFileManager videoFileManager;
	
	private final List<Analysis> analysisList;
	
	private final Frame parentFrame;

	public CleanupRecordingsTask(Frame parentFrame, List<Analysis> analysisList, VideoFileManager videoFileManager) {
		super("cleanupRecordings");
		this.parentFrame = parentFrame;
		this.videoFileManager = videoFileManager;
		this.analysisList = analysisList;
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		message("startMessage");
		List<File> filesToDelete = Lists.newArrayList();
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
			setProgress(getAnalysisList().indexOf(analysis), 0, getAnalysisList().size());
		}
		fileCount = filesToDelete.size();
		boolean success = fileCount >= 0;
		if (fileCount > 0) {
			int chosenOption = JOptionPane.showConfirmDialog(getParentFrame(), 
					getResourceString("filesFoundMessage", fileCount), 
					getResourceString("startMessage"), JOptionPane.OK_CANCEL_OPTION);
			if (chosenOption == JOptionPane.OK_OPTION) {
				for (File file : filesToDelete) {
					if (file.delete()) {
						filesDeleted++;
					} else {
						success = false;
					}
				}
			}
		}
		message("endMessage");
		return success;
	}

	@Override
	protected void finished() {
		try {
			String dialogMsg = getResourceString("finishedMessage", filesDeleted); 
			String dialogTitle = getResourceString("endMessage");
			if (fileCount == 0) {
				JOptionPane.showMessageDialog(parentFrame, 
						getResourceString("noFilesFoundMessage"), 
						dialogTitle, JOptionPane.INFORMATION_MESSAGE);
			} else if (get()) {
				JOptionPane.showMessageDialog(parentFrame,
						dialogMsg, dialogTitle, JOptionPane.PLAIN_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(parentFrame,
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

	public List<Analysis> getAnalysisList() {
		return analysisList;
	}

	public Frame getParentFrame() {
		return parentFrame;
	}
	
}