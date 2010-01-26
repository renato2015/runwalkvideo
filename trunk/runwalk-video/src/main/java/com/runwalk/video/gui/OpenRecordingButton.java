package com.runwalk.video.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.util.AppSettings;

@SuppressWarnings("serial")
public class OpenRecordingButton extends JButton {
	final Recording recording;
	
	public OpenRecordingButton(Recording recording) {
		super("open");
		this.recording = recording;
		setFont(AppSettings.MAIN_FONT);
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (isEnabled()) {
					RunwalkVideoApp.getApplication().getMediaControls().playFile(OpenRecordingButton.this.recording);
				}
			}
		});
	}

	@Override
	public boolean isEnabled() {
		return recording != null && recording.isRecorded();
	}
}
