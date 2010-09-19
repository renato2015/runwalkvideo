package com.runwalk.video.gui.tasks;

import java.io.File;

import ca.odell.glazedlists.EventList;

import com.runwalk.video.VideoFileManager;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;

public class RefreshVideoFilesTask extends AbstractTask<Boolean, Void> {

	private final EventList<Analysis> analysisList;
	private final VideoFileManager videoFileManager;

	public RefreshVideoFilesTask(VideoFileManager videoFileManager, EventList<Analysis> analysisList) {
		super("refreshVideoFiles");
		this.videoFileManager = videoFileManager;
		this.analysisList = analysisList;
	}

	protected Boolean doInBackground() throws Exception {
		// some not so beautiful way to refresh the cache
		message("startMessage");
		getAnalysisList().getReadWriteLock().readLock().lock();
		int progress = 0, filesMissing = 0;
		boolean compressable = false;
		try {
			for (Analysis analysis : getAnalysisList()) {
				for (Recording recording  : analysis.getRecordings()) {
					File videoFile = getVideoFileManager().refreshCache(recording);
					compressable |= recording.isCompressable();
					filesMissing = videoFile == null ? ++filesMissing : filesMissing;
				}
				setProgress(++progress, 0, getAnalysisList().size());
			}
		} finally {
			getAnalysisList().getReadWriteLock().readLock().unlock();
		}
		message("endMessage", filesMissing);
		return compressable;
	}
	
	public EventList<Analysis> getAnalysisList() {
		return analysisList;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

}
