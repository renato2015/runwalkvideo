package com.runwalk.video.ui;


import org.jdesktop.application.ResourceMap;

import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.model.RecordingModel;

public class RecordingModelTableFormat extends AbstractTableFormat<RecordingModel> {
	
	private VideoFileManager videoFileManager;
	
	public RecordingModelTableFormat(ResourceMap resourceMap, VideoFileManager videoFileManager) {
		super(resourceMap);
		this.videoFileManager = videoFileManager;
	}

	public Object getColumnValue(final RecordingModel recordingModel, int column) {
		// existance of the recording's video file should be checked by the videoFileManager upon load
		switch(column) {
		case 0: return recordingModel.getCreationDate();
		case 1: return recordingModel.getVideoFileName();
		case 2: return recordingModel.getKeyframeCount();
		case 3: return recordingModel.getDuration();
		case 4: {
			RecordingStatus recordingStatus = getVideoFileManager().getRecordingStatus(recordingModel.getEntity());
			return getResourceString(recordingStatus.getResourceKey());
		}
		case 5: return videoFileManager.isRecorded(recordingModel.getEntity());
		default: return null;
		}
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}
	
}