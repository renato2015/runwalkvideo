package com.runwalk.video.gui.tasks;

import java.util.List;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.VideoFileManager;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Analysis;

public class RefreshVideoFilesTask extends AbstractTask<Void, Void> {

	private final VideoFileManager videoFileManager;
	private final DaoService daoService;

	public RefreshVideoFilesTask(VideoFileManager videoFileManager, DaoService daoService) {
		super("refreshVideoFiles");
		this.videoFileManager = videoFileManager;
		this.daoService = daoService;
	}

	protected Void doInBackground() throws Exception {
		message("startMessage");
		// get all analyses from the db (not using derived list to enable multiple threads to work simultaneously)
		List<Analysis> analysisList = getDaoService().getDao(Analysis.class).getAll();
		int filesMissing = 0;
		for (Analysis analysis : analysisList) {
			filesMissing = filesMissing + getVideoFileManager().refreshCache(analysis);
			setProgress(analysisList.indexOf(analysis) + 1, 0, analysisList.size());
		}
		// check whether compressing should be enabled
		RunwalkVideoApp.getApplication().getAnalysisOverviewTablePanel().setCompressionEnabled(true);
		message("endMessage", filesMissing);
		return null;
	}
	
	public DaoService getDaoService() {
		return daoService;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

}
