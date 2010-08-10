package com.runwalk.video.gui.media;

import java.awt.Component;
import java.io.File;
import java.util.List;

public interface IVideoCapturer extends IVideoComponent {

	public static final String TIME_RECORDED = "timeRecorded";

	public void startRecording(File destFile);
	
	public void stopRecording();

	public void showCaptureSettings();

	public void showCameraSettings();

	public void setSelectedVideoFormatIndex(int selectedFormat);

	public List<String> getVideoFormats();

	public String getSelectedCaptureEncoderName();

	public void setSelectedCaptureEncoderIndex(int index);
	
	public List<String> getCaptureEncoders();

	public void togglePreview();

	/**
	 * Start running and bring the capturer to a state in which the {@link Component} returned by {@link #getComponent()} 
	 * or the {@link java.awt.Window} returned by {@link #getFullscreenFrame()} are initialized and ready to be shown on screen.
	 */
	public abstract void startCapturer();
	
}