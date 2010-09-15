package com.runwalk.video.gui.media;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.jdesktop.application.Action;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;

public class VideoCapturer extends VideoComponent {

	public static final String TIME_RECORDING = "timeRecorded";

	public static final String CAPTURE_ENCODER_NAME = "captureEncoderName";

	/**
	 * Keeps track of the total number of capturer instances
	 */
	private static int capturerCount = 0;
	
	private long timeStarted, timeRecorded;
	
	private IVideoCapturer videoImpl;

	VideoCapturer(PropertyChangeListener listener) {
		super(listener, ++capturerCount);
		setTimer(new Timer(1000, null));
		getTimer().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				long currentTime = System.currentTimeMillis();
				firePropertyChange(TIME_RECORDING, timeRecorded, timeRecorded = currentTime - timeStarted);
				getRecording().setDuration(timeRecorded);	
			}
		});
	}
	
	@Action
	public void togglePreview() {
		getVideoImpl().togglePreview();
	}

	@Action
	public void setCaptureEncoder() {
		List<String> captureEncoderNames = getVideoImpl().getCaptureEncoderNames();
		String captureEncoderName =  (String) JOptionPane.showInputDialog(
				null,
				"Kies een video encoder: ",
				"Video encoder wijzigen..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				Iterables.toArray(captureEncoderNames, String.class),
				getVideoImpl().getCaptureEncoderName());
		if (captureEncoderName != null) {
			firePropertyChange(CAPTURE_ENCODER_NAME, getVideoImpl().getCaptureEncoderName(), captureEncoderName);
			getVideoImpl().setCaptureEncoderName(captureEncoderName);
			getLogger().debug("Video encoder for " + getTitle() + " changed to " + getVideoImpl().getCaptureEncoderName());
		}
	}

	public IVideoCapturer getVideoImpl() {
		return videoImpl;
	}
	
	public void setVideoImpl(IVideoCapturer videoImpl) {
		this.videoImpl = videoImpl;
	}

	@Action
	@Override
	public void dispose() {
		super.dispose();
		capturerCount++;
		setVideoImpl(null);
	}

	public void startRecording(Recording recording, File videoFile) {
		if (videoFile == null || recording == null) {
			throw new IllegalArgumentException("No valid file or recording specified");
		} 
		setRecording(recording);
		timeStarted = System.currentTimeMillis();
		getVideoImpl().startRecording(videoFile);
		getLogger().debug("Recording to file " + videoFile.getAbsolutePath());
		recording.setRecordingStatus(RecordingStatus.RECORDING);
		getTimer().restart();
		setState(State.RECORDING);
	}

	public void stopRecording() {
		getTimer().stop();
		getVideoImpl().stopRecording();
		setState(State.IDLE);
		getRecording().setRecordingStatus(RecordingStatus.UNCOMPRESSED);
	}

	@Action
	public void showCapturerSettings() {
		getVideoImpl().showCaptureSettings();
	}

	@Action
	public void showCameraSettings() {
		getVideoImpl().showCameraSettings();
	}

	public boolean isRecording() {
		return getState() == State.RECORDING;
	}

	@Override
	public String getTitle() {
		return getResourceMap().getString("windowTitle.text", super.getTitle());
	}


}
