package com.runwalk.video.gui.media;

import java.awt.Component;
import java.io.File;

public interface IVideoCapturer extends IVideoComponent {

	public static final String TIME_RECORDED = "timeRecorded";

	public void startRecording(File destFile);
	
	public void stopRecording();

	public void showCaptureSettings();

	public void showCameraSettings();

	public int getSelectedCaptureDeviceIndex();

	public void setSelectedCaptureDeviceIndex(int selectedIndex);

	public void setSelectedVideoFormatIndex(int selectedFormat);

	public String[] getVideoFormats();

	public String getSelectedCaptureEncoderName();

	public void setSelectedCaptureEncoderIndex(int index);

	public String[] getCaptureEncoders();

	public String[] getCaptureDevices();

	/**
	 * Initialize the capturer and bring it to a state in which the {@link Component} returned by {@link #getComponent()} 
	 * or the {@link java.awt.Window} returned by {@link #getFullscreenFrame()} are running and ready to be added to a swing container.
	 */
	public void startCapturer();
	
	public void togglePreview();

}