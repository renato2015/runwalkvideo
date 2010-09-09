package com.runwalk.video.gui.tasks;

import java.awt.Frame;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.runwalk.video.VideoFileManager;
import com.runwalk.video.VideoFolderRetrievalStrategy;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;

public class OrganiseVideoFilesTask extends AbstractTask<Void, Void> {

	private final VideoFileManager videoFileManager;
	private final VideoFolderRetrievalStrategy videoFolderRetrievalStrategy;
	private final List<Analysis> analysisList;
	private final Frame parentFrame;

	public OrganiseVideoFilesTask(Frame parentFrame, List<Analysis> analysisList, 
			VideoFileManager videoFileManager, VideoFolderRetrievalStrategy videoFolderRetrievalStrategy) {
		super("organiseVideoFiles");
		this.parentFrame = parentFrame;
		this.videoFileManager = videoFileManager;
		this.videoFolderRetrievalStrategy = videoFolderRetrievalStrategy;
		this.analysisList = Lists.newArrayList(analysisList);
	}

	protected Void doInBackground() throws Exception {
		message("startMessage");
		File videoDir = getVideoFileManager().getVideoDir();
		int filesMoved = 0;
		for (Analysis analysis : analysisList) {
			for (Recording recording : analysis.getRecordings()) {
				File compressedVideoFile = getVideoFileManager().getCompressedVideoFile(recording);
				// check whether a compressed video file exists for the given recording
				if (compressedVideoFile.exists()) {
					File newFolder = getVideoFolderRetrievalStrategy().getVideoFolder(videoDir, recording);
					// copy the file to the new folder and create the directory if needed, maintaining file creation dates
					try {
						getLogger().debug("Moving video file " + recording.getVideoFileName() + " to " + newFolder);
						FileUtils.moveFileToDirectory(compressedVideoFile, newFolder, true);
						// delete cached file 
						getVideoFileManager().refreshCache(recording);
						filesMoved++;
					} catch (IOException e) {
						Logger.getLogger(VideoFileManager.class).error(e);
					}
				}
			}
			setProgress(getAnalysisList().indexOf(analysis), 0, getAnalysisList().size()-1);
		}
		getVideoFileManager().setVideoFolderRetrievalStrategy(videoFolderRetrievalStrategy);
		// delete empty directories after moving files to new folder structure
		int deletedDirectories = deleteEmptyDirectories(0, getVideoFileManager().getVideoDir());
		message("endMessage");
		JOptionPane.showMessageDialog(getParentFrame(), 
				getResourceString("filesMovedMessage", filesMoved, deletedDirectories), 
				getResourceString("startMessage"), JOptionPane.INFORMATION_MESSAGE);
		return null;
	}

	/**
	 * Recursively delete all empty directories found in a given directory.

	 * @param directoryCount Current count of deleted directories
	 * @param directory The directory to delete empty directories in
	 * @return The total number of deleted directories
	 */
	private int deleteEmptyDirectories(int directoryCount, File directory) {
		if( directory.exists() ) {
			File[] files = directory.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteEmptyDirectories(directoryCount, files[i]);
				} 
			}
		}
		return directory.delete() ? directoryCount++ : directoryCount;
	}

	@Override
	protected void failed(Throwable cause) {
		JOptionPane.showMessageDialog(getParentFrame(), getResourceMap().getString("errorMessage"),
				getResourceString("endMessage"), JOptionPane.ERROR_MESSAGE);
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public VideoFolderRetrievalStrategy getVideoFolderRetrievalStrategy() {
		return videoFolderRetrievalStrategy;
	}

	public List<Analysis> getAnalysisList() {
		return analysisList;
	}

	public Frame getParentFrame() {
		return parentFrame;
	}

}
