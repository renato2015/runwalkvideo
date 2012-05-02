package com.runwalk.video.media;

import java.util.List;

import javax.swing.JOptionPane;

import org.jdesktop.application.Action;

import com.google.common.collect.Iterables;
import com.runwalk.video.core.OnEdt;

public class VideoCapturer extends VideoComponent {

	public static final String TIME_RECORDING = "timeRecorded";

	public static final String CAPTURE_ENCODER_NAME = "captureEncoderName";

	/**
	 * Keeps track of the total number of capturer instances
	 */
	private static int capturerCount = 0;
	
	private IVideoCapturer videoImpl;
	
	VideoCapturer() {
		super(++capturerCount);
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
				getCaptureEncoderName());
		if (captureEncoderName != null) {
			firePropertyChange(CAPTURE_ENCODER_NAME, getCaptureEncoderName(), captureEncoderName);
			getVideoImpl().setCaptureEncoderName(captureEncoderName);
			getLogger().debug("Video encoder for " + getTitle() + " changed to " + getCaptureEncoderName());
		}
	}

	public IVideoCapturer getVideoImpl() {
		return videoImpl;
	}
	
	public void setVideoImpl(IVideoCapturer videoImpl) {
		this.videoImpl = videoImpl;
	}

	@OnEdt
	@Override
	public void dispose() {
		super.dispose();
		setVideoImpl(null);
		capturerCount--;
	}

	public void startRecording(String videoPath) {
		if (videoPath == null) {
			throw new IllegalArgumentException("No valid file or recording specified");
		} 
		setVideoPath(videoPath);
		getVideoImpl().startRecording(videoPath);
		getLogger().debug("Recording to file " + videoPath );
		setState(State.RECORDING);
	}

	public void stopRecording() {
		getVideoImpl().stopRecording();
		setIdle(true);
	}
	
	/**
	 * Return the currently used capture encoder name.
	 * @return The name of the encoder
	 */
	public String getCaptureEncoderName() {
		return getVideoImpl().getCaptureEncoderName();
	}
	
	@Action
	public void showCapturerSettings() {
		if (getVideoImpl().showCaptureSettings()) {
			setIdle(true);
		}
	}

	@Action
	public void showCameraSettings() {
		if (getVideoImpl().showCameraSettings()) {
			setIdle(true);
		}
	}

	public boolean isRecording() {
		return getState() == State.RECORDING;
	}

	@Override
	public String getTitle() {
		return getResourceMap().getString("windowTitle.text", super.getTitle());
	}


}
