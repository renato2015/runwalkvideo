package com.runwalk.video.gui.media;

import java.io.File;

public interface IVideoCapturer extends IVideoComponent {

	public static final String TIME_RECORDED = "timeRecorded";

	public void startRecording(File destFile);
	
	public void stopRecording();

	public void showCaptureSettings();

	public void showCameraSettings();

	public abstract int getSelectedCaptureDeviceIndex();

	public abstract void setSelectedCaptureDeviceIndex(int selectedIndex);

	public abstract void setSelectedVideoFormatIndex(int selectedFormat);

	public abstract String[] getVideoFormats();

	public abstract String getSelectedCaptureEncoderName();

	public abstract void setSelectedCaptureEncoderIndex(int index);

	public String[] getCaptureEncoders();

	public abstract String[] getCaptureDevices();

	public abstract void initCapturer();

}