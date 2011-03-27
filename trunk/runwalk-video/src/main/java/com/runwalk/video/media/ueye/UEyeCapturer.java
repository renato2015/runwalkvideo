package com.runwalk.video.media.ueye;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import com.runwalk.video.media.IVideoCapturer;
import com.sun.jna.ptr.IntByReference;

class UEyeCapturer implements IVideoCapturer {

	private static final String MJPEG_ENCODER = "MJPEG";

	private static Logger LOGGER =  Logger.getLogger(UEyeCapturer.class);

	private final String cameraName;
	private IntByReference cameraHandle;
	private IntByReference aviHandle;

	private File settingsFile;

	UEyeCapturer(int cameraId, String cameraName) {
		this.cameraName = cameraName;
		cameraHandle = new IntByReference(cameraId);
		int result = UEyeCapturerLibrary.InitializeCamera(cameraHandle);
		LOGGER.debug("InitializeCamera result = " + result);
		LOGGER.debug("Camera handle value = "  + cameraHandle.getValue());
	}

	public String getTitle() {
		return cameraName;
	}

	public void dispose() {
		// set all handles to null
		cameraHandle = null;
		aviHandle = null;
	}

	public boolean isActive() {
		return cameraHandle != null;
	}

	public Dimension getDimension() {
		// TODO eventually read dimensions from settings file??
		return null;
	}

	public void setFullScreen(GraphicsDevice graphicsDevice, boolean b) {
		// capturer always runs fullscreen
	}

	public void startRunning() {
		LOGGER.debug("Opening camera " + getTitle());
		int result = UEyeCapturerLibrary.StartRunning(cameraHandle, settingsFile.getAbsolutePath(), getTitle());
		LOGGER.debug("Using settings file at " + settingsFile.getAbsolutePath());
		LOGGER.debug("StartRunning result = " + result);
	}

	public void stopRunning() {
		int result = UEyeCapturerLibrary.StopRunning(cameraHandle);
		LOGGER.debug("StopRunning result = " + result);
	}

	public void setOverlayImage(BufferedImage image, Color alphaColor) {
		// not implemented (yet?)
	}

	public BufferedImage getImage() {
		// TODO can be implemented later on
		return null;
	}

	public void startRecording(File destFile) {
		aviHandle = new IntByReference(0);
		int result = UEyeCapturerLibrary.StartRecording(cameraHandle, aviHandle, destFile.getAbsolutePath(), 25);
		System.out.println("startRecording result: "+ result);
	}

	public void stopRecording() {
		int result = UEyeCapturerLibrary.StopRecording(aviHandle.getValue());
		LOGGER.debug("StopRecording result = " + result);
	}

	/**
	 * This implementation will open the .ini settings file for the selected camera.
	 */
	public void showCaptureSettings() {
		// nothing to show here
		if (settingsFile != null && settingsFile.exists()) {
			try {
				Desktop desktop = null;
				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
					desktop.edit(settingsFile);
				}
			} catch (IOException e) {
				LOGGER.error("Failed to open settings file with default editor", e);
			}
		}
	}

	@Override
	public void showCameraSettings() {
		// show a filechooser dialog which enables you to select a settings file
		final JFileChooser chooser = settingsFile == null ? new JFileChooser() : new JFileChooser(settingsFile);
		chooser.setFileFilter(new FileFilter() {

			public boolean accept(File f) {
				return f.getName().endsWith(".ini") || f.isDirectory();
			}

			public String getDescription() {
				return "uEye parameter files";
			}
			
		});
		int returnVal = chooser.showDialog(null, "Kies");
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			settingsFile = chooser.getSelectedFile();
		}
	}

	@Override
	public String getCaptureEncoderName() {
		return MJPEG_ENCODER;
	}

	@Override
	public void setCaptureEncoderName(String name) {
		// do nothing, this can only be MJPEG
	}

	public List<String> getCaptureEncoderNames() {
		return Collections.singletonList(MJPEG_ENCODER);
	}

	public Frame getFullscreenFrame() {
		return null;
	}

	public Component getComponent() {
		return null;
	}

}
