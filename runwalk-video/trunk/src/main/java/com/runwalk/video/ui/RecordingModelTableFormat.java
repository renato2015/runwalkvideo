package com.runwalk.video.ui;

import org.jdesktop.application.ResourceMap;

import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.model.RecordingModel;

public class RecordingModelTableFormat extends AbstractTableFormat<RecordingModel> {
	
	public RecordingModelTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
	}

	public Object getColumnValue(final RecordingModel recordingModel, int column) {
		// existance of the recording's video file should be checked by the videoFileManager upon load
		switch(column) {
		case 0: return 1;//recordingModel.getRecordingCount();
		case 1: return recordingModel.getCreationDate();
		case 2: return null;//analysisModel.getClient().toString();
		case 3: return recordingModel.getKeyframeCount();
		case 4: return recordingModel.getDuration();
		case 5: {
			String result = getResourceString(RecordingStatus.NONE.getResourceKey());
			RecordingStatus recordingStatus = recordingModel.getRecordingStatus();
			if (recordingStatus != null) {
				result = getResourceString(recordingStatus.getResourceKey());
			}
			return result;
		}
		case 6: return recordingModel.isRecorded();
		default: return null;
		}
	}
}