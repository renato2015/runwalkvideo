package com.runwalk.video.gui.tasks;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;

import com.runwalk.video.VideoFileManager;
import com.runwalk.video.VideoFolderRetrievalStrategy;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;

public class OrganiseVideoFilesTask extends AbstractTask<Void, Void> {

	private final VideoFileManager videoFileManager;
	private final VideoFolderRetrievalStrategy videoFolderRetrievalStrategy;
	private final EventList<Analysis> analysisList;
	private final Component parentComponent;
	private int deletedDirectories = 0, filesMoved = 0;

	public OrganiseVideoFilesTask(Component parentComponent, EventList<Analysis> analysisList, 
			VideoFileManager videoFileManager, VideoFolderRetrievalStrategy videoFolderRetrievalStrategy) {
		super("organiseVideoFiles");
		this.parentComponent = parentComponent;
		this.videoFileManager = videoFileManager;
		this.videoFolderRetrievalStrategy = videoFolderRetrievalStrategy;
		this.analysisList = analysisList;
	}

	protected Void doInBackground() throws Exception {
		message("startMessage");
		File videoDir = getVideoFileManager().getVideoDir();
		getAnalysisList().getReadWriteLock().readLock().lock();
		try {
			int progress = 0;
			for (Analysis analysis : getAnalysisList()) {
				for (Recording recording : analysis.getRecordings()) {
					File compressedVideoFile = getVideoFileManager().getCompressedVideoFile(recording);
					// check whether a compressed video file exists for the given recording
					if (compressedVideoFile.exists()) {
						File newFolder = getVideoFolderRetrievalStrategy().getVideoFolder(videoDir, recording);
						if (!newFolder.equals(compressedVideoFile.getParentFile())) {
							// copy the file to the new folder and create the directory if needed, maintaining file creation dates
							try {
								getLogger().debug("Moving video file " + 
										recording.getVideoFileName() + " to " + newFolder);
								FileUtils.moveFileToDirectory(compressedVideoFile, newFolder, true);
								filesMoved++;
							} catch (IOException e) {
								Logger.getLogger(VideoFileManager.class).error(e);
							}
						}
					}
					// refresh video file cache using new strategy
					getVideoFileManager().refreshCache(getVideoFolderRetrievalStrategy(), recording);
				}
				setProgress(++progress, 0, getAnalysisList().size());
			}
			getVideoFileManager().setVideoFolderRetrievalStrategy(getVideoFolderRetrievalStrategy());
		} finally {
			getAnalysisList().getReadWriteLock().readLock().unlock();
		}
		// delete empty directories after moving files to new folder structure
		deletedDirectories = deleteEmptyDirectories(getVideoFileManager().getVideoDir());
		message("endMessage");
		return null;
	}

	@Override
	protected void finished() {
		JOptionPane.showMessageDialog(getParentComponent(), 
				getResourceString("filesMovedMessage", filesMoved, deletedDirectories), 
				getResourceString("startMessage"), JOptionPane.INFORMATION_MESSAGE);
	}

	private int deleteEmptyDirectories(File directory) {
		return deleteEmptyDirectories(0, directory);
	}
	
	/**
	 * Recursively delete all empty directories found in a given directory.
	 * Invisible directories and subdirectories will be ignored.
	 *
	 * @param directory The directory to delete empty directories in
	 * @param foldersDeleted The number of currently deleted folders, should be 0 on invocation
	 * @return The total number of deleted directories
	 */
	private int deleteEmptyDirectories(int foldersDeleted, File directory) {
		if( directory.exists() ) {
			File[] files = directory.listFiles();
			for(int i = 0; i < files.length; i++) {
				// don't delete invisible directories
				if (files[i].isDirectory() && files[i].getName().charAt(0) != '.') {
					foldersDeleted = deleteEmptyDirectories(foldersDeleted, files[i]);
				} 
			}
		}
		// we need a pre increment here, so the variable will be incremented before it is returned
		return directory.delete() ? ++foldersDeleted : foldersDeleted;
	}

	@Override
	protected void failed(Throwable cause) {
		JOptionPane.showMessageDialog(getParentComponent(), getResourceMap().getString("errorMessage"),
				getResourceString("endMessage"), JOptionPane.ERROR_MESSAGE);
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public VideoFolderRetrievalStrategy getVideoFolderRetrievalStrategy() {
		return videoFolderRetrievalStrategy;
	}

	public EventList<Analysis> getAnalysisList() {
		return analysisList;
	}

	public Component getParentComponent() {
		return parentComponent;
	}

}
