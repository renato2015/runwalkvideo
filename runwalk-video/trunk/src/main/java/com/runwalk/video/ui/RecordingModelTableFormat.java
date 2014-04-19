package com.runwalk.video.ui;

import org.jdesktop.application.ResourceMap;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.model.AnalysisModel;

public class RecordingModelTableFormat extends AbstractTableFormat<AnalysisModel> {
	
	public RecordingModelTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
	}

	public Object getColumnValue(final AnalysisModel recordingModel, int column) {
		// existance of the recording's video file should be checked by the videoFileManager upon load
		final boolean recordingNotNull = recordingModel.isRecordingsEmpty();
		Recording recording = null;
		if (recordingNotNull) {
			recording = Iterables.getLast(recordingModel.getRecordings());
		}
		switch(column) {
		case 0: return recordingNotNull ? recordingModel.getRecordingCount() : null;
		case 1: return recordingModel.getCreationDate();
		case 2: return null;//analysisModel.getClient().toString();
		case 3: return recordingNotNull ? recording.getKeyframeCount() : 0;
		case 4: return recordingNotNull ? recording.getDuration() : 0L;
		case 5: {
			String result = getResourceString(RecordingStatus.NONE.getResourceKey());
			RecordingStatus recordingStatus = recording.getRecordingStatus();
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