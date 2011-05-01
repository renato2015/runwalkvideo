package com.runwalk.video.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JButton;

import org.jdesktop.application.ActionManager;

import ca.odell.glazedlists.gui.WritableTableFormat;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Article;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.util.AppSettings;

public class AnalysisTableFormat implements WritableTableFormat<Analysis> {
	
	private final Action openRecordingsAction;
	
	private final JButton openRecordingsButton;

	public AnalysisTableFormat(Action openRecordingsAction) {
		this.openRecordingsAction = openRecordingsAction;
		openRecordingsButton = new JButton("open");
		openRecordingsButton.setFont(AppSettings.MAIN_FONT);
	}
	
	public int getColumnCount() {
		return 5;
	}

	public String getColumnName(int column) {
		if(column == 0)      return "Datum";
		else if(column == 1) return "Gekozen schoen";
		else if(column == 2) return "Aantal keyframes";
		else if(column == 3) return "Aantal opnames";
		else if(column == 4) return "Open video";
		throw new IllegalStateException();
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
			final JButton button = new JButton("open");
    		button.setFont(AppSettings.MAIN_FONT);
    		button.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					
				}
    			
    		});
    		button.addMouseListener(new MouseAdapter() {
    			public void mouseClicked(MouseEvent e) {
    				ActionManager.invokeAction(getOpenRecordingsAction(), button);
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
	
	private Action getOpenRecordingsAction() {
		return openRecordingsAction;
	}
}
