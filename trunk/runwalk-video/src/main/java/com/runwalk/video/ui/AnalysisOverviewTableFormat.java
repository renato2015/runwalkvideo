package com.runwalk.video.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import ca.odell.glazedlists.gui.TableFormat;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.media.MediaControls;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.ResourceInjector;

public class AnalysisOverviewTableFormat implements TableFormat<Analysis> {
	
	private final MediaControls mediaControls;

	public AnalysisOverviewTableFormat(MediaControls mediaControls) {
		this.mediaControls = mediaControls;
	}

	public int getColumnCount() {
		return 7;
	}

	public String getColumnName(int column) {
		if(column == 0)      return "#";
		else if(column == 1) return "Tijdstip analyse";
		else if(column == 2) return "Naam klant";
		else if(column == 3) return "Aantal keyframes";
		else if(column == 4) return "Duur video";
		else if(column == 5) return "Status";
		else if(column == 6) return "";
		throw new IllegalStateException();
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
			String result = "<geen>";
			RecordingStatus recordingStatus = recording.getRecordingStatus();
			if (recordingStatus != null) {
				String resourceKey = recordingStatus.getResourceKey();
				ResourceInjector resourceInjector = ResourceInjector.getInstance();
				result = resourceInjector.injectResources(resourceKey, RecordingStatus.class);
			}
			return result;
		}
		case 6: {
			// FIXME could be a possible memory leak..
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
	
	private MediaControls getMediaControls() {
		return mediaControls;
	}

}
