package com.runwalk.video.ui;

import java.util.Date;

import org.jdesktop.application.ResourceMap;

import ca.odell.glazedlists.gui.WritableTableFormat;

import com.runwalk.video.entities.Analysis.Progression;
import com.runwalk.video.entities.Item;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.model.AnalysisModel;

public class AnalysisModelTableFormat extends AbstractTableFormat<AnalysisModel> implements WritableTableFormat<AnalysisModel> {
	
	private VideoFileManager videoFileManager;

	public AnalysisModelTableFormat(ResourceMap resourceMap, VideoFileManager videoFileManager) {
		super(resourceMap);
		this.videoFileManager = videoFileManager;
	}
	
	public Object getColumnValue(final AnalysisModel analysisModel, int column) {
		switch(column) {
		case 0: return analysisModel.getCreationDate();
		case 1: return analysisModel.getItem();
		case 2: return analysisModel.getProgression();
		case 3: return analysisModel.getDuration();	
		case 4: return analysisModel.getComments();
		case 5: return getVideoFileManager().isRecorded(analysisModel.getRecordings());
		default: return null;
		}
	}

	public boolean isEditable(AnalysisModel baseObject, int column) {
		return (baseObject.getFeedbackId() != null && baseObject.getTokenId() == null && column == 0) || 
				(column == 1 && baseObject.getFeedbackId() == null) || 
				baseObject.getFeedbackId() == null && column == 2;
	}

	public AnalysisModel setColumnValue(AnalysisModel analysisModel, Object editedValue, int column) {
		if (editedValue instanceof Item) {
			analysisModel.setItem((Item) editedValue);
		}
		if (column == 0) {
			analysisModel.setCreationDate((Date) editedValue);
		} else if (column == 2) {
			analysisModel.setProgression((Progression) editedValue);
		}
		return analysisModel;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}
	
}
