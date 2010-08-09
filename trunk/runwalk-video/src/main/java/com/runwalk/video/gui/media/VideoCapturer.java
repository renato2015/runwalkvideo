package com.runwalk.video.gui.media;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.jdesktop.application.Action;

import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;

public class VideoCapturer extends VideoComponent {

	protected static final String TIME_RECORDING = "timeRecorded";

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
	public void setVideoFormat() {
		String[] formats = getVideoImpl().getVideoFormats();
		String selectedFormat = (String)JOptionPane.showInputDialog(
				null,
				"Kies het opnameformaat:",
				"Kies opnameformaat..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				formats,
				formats[0]);
		if (selectedFormat != null) {
			int selectedIndex = Arrays.asList(formats).indexOf(selectedFormat);
			getVideoImpl().setSelectedVideoFormatIndex(selectedIndex);
			getLogger().debug("Video format for " + getTitle() + " set to " + selectedFormat);
		}
	}

	@Action
	public void togglePreview() {
		getVideoImpl().togglePreview();
	}

	@Action
	public void setCaptureEncoder() {
		String[] captureEncoders = getVideoImpl().getCaptureEncoders();
		String selectedEncoder =  (String) JOptionPane.showInputDialog(
				null,
				"Kies een video encoder: ",
				"Video encoder wijzigen..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				captureEncoders,
				getVideoImpl().getSelectedCaptureEncoderName());
		if (selectedEncoder != null) {
			int selectedIndex = Arrays.asList(captureEncoders).indexOf(selectedEncoder);
			getVideoImpl().setSelectedCaptureEncoderIndex(selectedIndex);
			getLogger().debug("Video encoder for " + getTitle() + " changed to " + getVideoImpl().getSelectedCaptureEncoderName());
		}
	}

	public IVideoCapturer getVideoImpl() {
		return videoImpl;
	}
	
	public void setVideoImpl(IVideoCapturer videoImpl) {
		this.videoImpl = videoImpl;
	}

	public void startRecording(Recording recording, File videoFile) {
		if (videoFile == null || recording == null) {
			throw new IllegalArgumentException("No valid file or recording specified");
		} 
		setVideoFile(videoFile);
		setRecording(recording);
		
		getApplication().getStatusPanel().setIndeterminate(true);
		timeStarted = System.currentTimeMillis();
		
		getVideoImpl().startRecording(videoFile);
		recording.setRecordingStatus(RecordingStatus.RECORDING);
		getLogger().debug("Recording to file " + videoFile.getAbsolutePath() + "");

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
