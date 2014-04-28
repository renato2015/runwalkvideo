package com.runwalk.video.tasks;

import java.awt.Robot;
import java.io.File;
import java.util.Set;

import com.runwalk.video.entities.Recording;
import com.runwalk.video.io.VideoFileManager;

public class RefreshVideoFilesTask extends AbstractTask<Boolean, Void> {

	private final VideoFileManager videoFileManager;

	public RefreshVideoFilesTask(VideoFileManager videoFileManager) {
		super("refreshVideoFiles");
		this.videoFileManager = videoFileManager;
	}

	protected Boolean doInBackground() throws Exception {
		message("startMessage");
		int progress = 0, filesMissing = 0;
		boolean compressable = false;
		Set<Recording> cachedRecordings = getVideoFileManager().getCachedRecordings();
		for (Recording recording  : cachedRecordings) {
			File videoFile = getVideoFileManager().getVideoFile(recording);
			compressable |= getVideoFileManager().canReadAndExists(videoFile);
			filesMissing = videoFile == null ? ++filesMissing : filesMissing;
		}
		setProgress(++progress, 0, cachedRecordings.size() + 1);
		message("waitForIdleMessage");
		new Robot().waitForIdle();
		setProgress(100);
		message("endMessage", filesMissing);
		return compressable;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

}
