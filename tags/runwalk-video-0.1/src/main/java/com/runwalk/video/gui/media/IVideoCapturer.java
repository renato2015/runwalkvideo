package com.runwalk.video.gui.media;

import java.awt.Component;
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

	/**
	 * This method simply invokes {@link #startCapturer()} if the capturer is stopped 
	 * or {@link #stopCapturer()} if the capturer is started at invocation time.
	 */
	public void togglePreview();

	/**
	 * Start running and bring the capturer to a state in which the {@link Component} returned by {@link #getComponent()} 
	 * or the {@link java.awt.Window} returned by {@link #getFullscreenFrame()} are initialized and showing live video.
	 */
	public abstract void startCapturer();

	/**
	 * Stop running and bring the capturer to a state in which video format settings can be applied. 
	 * In most cases the capturer will have to stop previewing video in order to reconfigure properly.
	 */
	public abstract void stopCapturer();
	
}