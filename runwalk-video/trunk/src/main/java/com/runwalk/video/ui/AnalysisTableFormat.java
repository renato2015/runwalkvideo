package com.runwalk.video.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import org.jdesktop.application.ResourceMap;

import ca.odell.glazedlists.gui.WritableTableFormat;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Article;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.media.MediaControls;
import com.runwalk.video.util.AppSettings;

public class AnalysisTableFormat extends AbstractTableFormat<Analysis> implements WritableTableFormat<Analysis> {
	
	private final MediaControls mediaControls;
	
	public AnalysisTableFormat(MediaControls mediaControls, ResourceMap resourceMap) {
		super(resourceMap);
		this.mediaControls = mediaControls;
	}
	
	public Object getColumnValue(final Analysis analysis, int column) {
		final boolean recordingNotNull = analysis.hasRecordings();
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
		case 4: {
			JButton button = new JButton("open");
    		button.setFont(AppSettings.MAIN_FONT);
    		button.addMouseListener(new MouseAdapter() {
    			public void mouseClicked(MouseEvent e) {
					getMediaControls().openRecordings(analysis);
    			}
    		});
    		button.setEnabled(analysis.isRecorded());
    		return button;
		}
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
	
	private MediaControls getMediaControls() {
		return mediaControls;
	}
}
