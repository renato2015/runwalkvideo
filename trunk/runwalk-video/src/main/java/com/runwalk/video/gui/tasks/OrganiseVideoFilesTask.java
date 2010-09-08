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
				if (recording.isCompressed()) {
					File compressedVideoFile = getVideoFileManager().getCompressedVideoFile(recording);
					// check whether a compressed video file exists for the given recording
					if (compressedVideoFile.exists()) {
						File newFolder = getVideoFolderRetrievalStrategy().getVideoFolder(videoDir, recording);
						// copy the file to the new folder and create the directory if needed, maintaining file creation dates
						try {
							FileUtils.moveFileToDirectory(compressedVideoFile, newFolder, true);
							filesMoved++;
						} catch (IOException e) {
							Logger.getLogger(VideoFileManager.class).error(e);
						}
					}
				}
			}
			setProgress(getAnalysisList().indexOf(analysis), 0, getAnalysisList().size()-1);
		}
		getVideoFileManager().setVideoFolderRetrievalStrategy(videoFolderRetrievalStrategy);
		// delete empty directories after moving files to new folder structure
		List<File> deletedDirectories = new EmptyDirectoryWalker().start(getVideoFileManager().getVideoDir());
		message("endMessage");
		JOptionPane.showMessageDialog(getParentFrame(), 
				getResourceString("filesMovedMessage", filesMoved, deletedDirectories.size()), 
				getResourceString("startMessage"), JOptionPane.INFORMATION_MESSAGE);
		return null;
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

	private class EmptyDirectoryWalker extends DirectoryWalker {

		private EmptyDirectoryWalker() {
			// don't process files, just find empty directories
			super(new FileFilter() {

				public boolean accept(File file) {
					return !file.isFile();
				}
				
			}, -1) ;
		}
		
		public List<File> start(File startDirectory) throws IOException {
			List<File> results = new ArrayList<File>();
			walk(startDirectory, results);
			return results;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		protected boolean handleDirectory(File directory, int depth, Collection results) {
			// find empty directories
			if (directory.listFiles().length == 0) {
				if (directory.delete()) {
					results.add(directory);
				}
				return false;
			}
			return true;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void handleDirectoryEnd(File directory, int depth, Collection results) throws IOException {
			// check if parent directory became empty after going through child
			File parentDirectory = directory.getParentFile();
			// if parent directory became empty, then delete it
			if (parentDirectory.listFiles().length == 0) {
				if (parentDirectory.delete()) {
					results.add(parentDirectory);
				}
			}
		}

	}

}
