package com.runwalk.video.media;

import java.io.File;
import java.util.List;

public interface IVideoCapturer extends IVideoComponent {

	public static final String TIME_RECORDED = "timeRecorded";

	/**
	 * Start recording to the specified {@link File}. 
	 * @param destFile The file to use for capturing
	 */
	public void startRecording(File destFile);
	
	public void stopRecording();

	public void showCaptureSettings();

	public void showCameraSettings();

	/**
	 * Return the name of the currently chosen encoder for capturing video with.
	 * @return The name of the encoder
	 */
	public String getCaptureEncoderName();

	/**
	 * Set the name of the encoder to use for capturing video.
	 * @param name The name of the encoder
	 */
	public void setCaptureEncoderName(String name);
	
	/**
	 * Return a list with all the available capture encoder names.
	 * @return The list with capture encoder names
	 */
	public List<String> getCaptureEncoderNames();

}