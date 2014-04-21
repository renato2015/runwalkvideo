package com.runwalk.video.tasks;

import java.awt.Robot;
import java.io.File;

import ca.odell.glazedlists.EventList;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.io.VideoFileManager;

public class RefreshVideoFilesTask extends AbstractTask<Boolean, Void> {

	private final EventList<Recording> recordingList;
	private final VideoFileManager videoFileManager;

	public RefreshVideoFilesTask(VideoFileManager videoFileManager, EventList<Recording> recordingList) {
		super("refreshVideoFiles");
		this.videoFileManager = videoFileManager;
		this.recordingList = recordingList;
	}

	protected Boolean doInBackground() throws Exception {
		message("startMessage");
		int progress = 0, filesMissing = 0;
		boolean compressable = false;
		getRecordingList().getReadWriteLock().readLock().lock();
		try {
			for (Recording recording  : recordingList) {
				File videoFile = getVideoFileManager().getVideoFile(recording);
				compressable |= getVideoFileManager().canReadAndExists(videoFile);
				filesMissing = videoFile == null ? ++filesMissing : filesMissing;
			}
			setProgress(++progress, 0, getRecordingList().size() + 1);
		} finally {
			getRecordingList().getReadWriteLock().readLock().unlock();
		}
		message("waitForIdleMessage");
		new Robot().waitForIdle();
		setProgress(100);
		message("endMessage", filesMissing);
		return compressable;
	}

	public EventList<Recording> getRecordingList() {
		return recordingList;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

}
