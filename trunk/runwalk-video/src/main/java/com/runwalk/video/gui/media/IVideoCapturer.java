package com.runwalk.video.gui.media;

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

	public void startCapturer();
	
	public void togglePreview();

}