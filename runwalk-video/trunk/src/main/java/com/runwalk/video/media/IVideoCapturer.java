package com.runwalk.video.media;

import java.io.File;
import java.util.List;

public interface IVideoCapturer extends IVideoComponent {

	String TIME_RECORDED = "timeRecorded";

	/**
	 * Start recording to the specified {@link File}. 
	 * @param videoPath The file to use for capturing
	 */
	void startRecording(String videoPath);
	
	void stopRecording();

	/**
	 * Show a settings dialog box. Will return <code>true</code>
	 * if the component's state was changed to running.
	 * @return <code>true</code> if the component is running
	 */
	boolean showCapturerSettings();
	
	/**
	 * Show a camera settings dialog box. Will return <code>true</code>
	 * if the component's state was changed to running.
	 * @return <code>true</code> if the component is running
	 */
	boolean showCameraSettings();

	/**
	 * Return the name of the currently chosen encoder for capturing video with.
	 * @return The name of the encoder
	 */
	String getCaptureEncoderName();

	/**
	 * Set the name of the encoder to use for capturing video.
	 * @param name The name of the encoder
	 */
	void setCaptureEncoderName(String name);
	
	/**
	 * Return a list with all the available capture encoder names.
	 * @return The list with capture encoder names
	 */
	List<String> getCaptureEncoderNames();

}