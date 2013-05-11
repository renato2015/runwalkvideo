package com.runwalk.video.ui;

import org.jdesktop.application.ResourceMap;

import ca.odell.glazedlists.gui.WritableTableFormat;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Article;
import com.runwalk.video.entities.Recording;

public class AnalysisTableFormat extends AbstractTableFormat<Analysis> implements WritableTableFormat<Analysis> {
	
	public AnalysisTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
	}
	
	public Object getColumnValue(final Analysis analysis, int column) {
		final boolean recordingNotNull = analysis.isRecordingsEmpty();
		Recording recording = null;
		if (recordingNotNull) {
			recording = Iterables.getLast(analysis.getRecordings());
		}
		switch(column) {
		case 0: return analysis.getCreationDate();
		case 1: return analysis.getArticle();
		case 2: {
			return recordingNotNull ? recording.getKeyframeCount() : 0;
		}
		case 3: return recordingNotNull ? recording.getDuration() : 0L;
		case 4: return analysis.getComments();
		case 5: return analysis.isRecorded();
		default: return null;
		}
	}

	public boolean isEditable(Analysis baseObject, int column) {
		return column == 1;
	}

	public Analysis setColumnValue(Analysis analysis, Object editedValue, int column) {
		if (editedValue instanceof Article) {
			analysis.setArticle((Article) editedValue);
		}
		return analysis;
	}
	
}
