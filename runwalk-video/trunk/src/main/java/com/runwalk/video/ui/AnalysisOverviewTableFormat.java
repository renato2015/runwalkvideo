package com.runwalk.video.ui;

import org.jdesktop.application.ResourceMap;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.util.ResourceInjector;

public class AnalysisOverviewTableFormat extends AbstractTableFormat<Analysis> {
	
	public AnalysisOverviewTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
	}

	public Object getColumnValue(final Analysis analysis, int column) {
		// existance of the recording's video file should be checked by the videoFileManager upon load
		final boolean recordingNotNull = analysis.hasRecordings();
		Recording recording = null;
		if (recordingNotNull) {
			recording = Iterables.getLast(analysis.getRecordings());
		}
		switch(column) {
		case 0: return recordingNotNull ? analysis.getRecordings().size() : null;
		case 1: return analysis.getCreationDate();
		case 2: return analysis.getClient().toString();
		case 3: return recordingNotNull ? recording.getKeyframeCount() : 0;
		case 4: return recordingNotNull ? recording.getDuration() : 0L;
		case 5: {
			ResourceInjector resourceInjector = ResourceInjector.getInstance();
			String result = resourceInjector.injectResources("status.none", RecordingStatus.class);
			RecordingStatus recordingStatus = recording.getRecordingStatus();
			if (recordingStatus != null) {
				String resourceKey = recordingStatus.getResourceKey();
				result = resourceInjector.injectResources(resourceKey, RecordingStatus.class);
			}
			return result;
		}
		case 6: return analysis.isRecorded();
		default: return null;
		}
	}
}
