package com.runwalk.video.gui.tasks;

import ca.odell.glazedlists.EventList;

import com.runwalk.video.VideoFileManager;
import com.runwalk.video.entities.Analysis;

public class RefreshVideoFilesTask extends AbstractTask<Void, Void> {

	private final EventList<Analysis> analysisList;
	private final VideoFileManager videoFileManager;

	public RefreshVideoFilesTask(VideoFileManager videoFileManager, EventList<Analysis> analysisList) {
		super("refreshVideoFiles");
		this.videoFileManager = videoFileManager;
		this.analysisList = analysisList;
	}

	protected Void doInBackground() throws Exception {
		// some not so beautiful way to refresh the cache
		message("startMessage");
		getAnalysisList().getReadWriteLock().readLock().lock();
		try {
			int progress = 0;
			for (Analysis analysis : getAnalysisList()) {
				getVideoFileManager().refreshCache(analysis);
				setProgress(++progress, 0, getAnalysisList().size());
			}
		} finally {
			getAnalysisList().getReadWriteLock().readLock().unlock();
		}
		message("endMessage");
		return null;
	}
	
	public EventList<Analysis> getAnalysisList() {
		return analysisList;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

}
