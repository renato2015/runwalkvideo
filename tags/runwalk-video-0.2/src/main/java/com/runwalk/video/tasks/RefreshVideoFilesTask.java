package com.runwalk.video.tasks;

import java.awt.Robot;
import java.io.File;

import ca.odell.glazedlists.EventList;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.io.VideoFileManager;

public class RefreshVideoFilesTask extends AbstractTask<Boolean, Void> {

	private final EventList<Analysis> analysisList;
	private final VideoFileManager videoFileManager;

	public RefreshVideoFilesTask(VideoFileManager videoFileManager, EventList<Analysis> analysisList) {
		super("refreshVideoFiles");
		this.videoFileManager = videoFileManager;
		this.analysisList = analysisList;
	}

	protected Boolean doInBackground() throws Exception {
		message("startMessage");
		int progress = 0, filesMissing = 0;
		boolean compressable = false;
		getAnalysisList().getReadWriteLock().readLock().lock();
		try {
			for (Analysis analysis : getAnalysisList()) {
				for (Recording recording  : analysis.getRecordings()) {
					File videoFile = getVideoFileManager().refreshCache(recording);
					compressable |= recording.isCompressable();
					filesMissing = videoFile == null ? ++filesMissing : filesMissing;
				}
				setProgress(++progress, 0, getAnalysisList().size() + 1);
			}
		} finally {
			getAnalysisList().getReadWriteLock().readLock().unlock();
		}
		message("waitForIdleMessage");
		new Robot().waitForIdle();
		setProgress(100);
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
