package com.runwalk.video.media.ueye;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;

import com.runwalk.video.core.PropertyChangeSupport;
import com.runwalk.video.core.SelfContained;
import com.runwalk.video.media.IVideoCapturer;
import com.runwalk.video.media.ueye.UEyeCapturerLibrary.OnWndShowCallback;
import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

public class UEyeCapturer implements IVideoCapturer, PropertyChangeSupport, SelfContained  {

	private static final String NO_SETTINGS_FILE = "<default>";

	public static final String UEYE_SETTINGS_PROPERTY = "ueye_settings_file";

	private static final String MJPEG_ENCODER = "MJPEG";

	private static Logger LOGGER =  Logger.getLogger(UEyeCapturer.class);

	private final String cameraName;
	private IntByReference cameraHandle;
	private IntByReference aviHandle;

	private File settingsFile;

	/** This hook can be used by native code to call back into java */
	private Callback callback = new OnWndShowCallback() {

		public void invoke(final boolean visible) {
			firePropertyChange(VISIBLE, UEyeCapturer.this.visible, UEyeCapturer.this.visible = visible);
		}

	};

	private Integer monitorId;

	private boolean visible = false;

	private volatile boolean recording = false;

	UEyeCapturer(int cameraId, String cameraName) {
		this.cameraName = cameraName;
		cameraHandle = new IntByReference(cameraId);
		aviHandle = new IntByReference(0);
		int result = UEyeCapturerLibrary.InitializeCamera(cameraHandle);
		LOGGER.debug("InitializeCamera result = " + result);
		LOGGER.debug("Camera handle value = "  + cameraHandle.getValue());
	}

	public String getTitle() {
		return cameraName;
	}

	public void dispose() {
		stopRunning();
		// set all handles to null
		cameraHandle = null;
		aviHandle = null;
		callback = null;
	}

	public boolean isActive() {
		return cameraHandle != null;
	}

	public Dimension getDimension() {
		// TODO eventually read dimensions from settings file??
		return null;
	}

	public void startRunning() {
		LOGGER.debug("Opening camera " + getTitle());
		IntByReference monitorId = new IntByReference(getMonitorId());
		String settingsFilePath = getSettingsFilePath();
		char[] windowName = Native.toCharArray(getTitle());
		int result = UEyeCapturerLibrary.StartRunning(cameraHandle, settingsFilePath, windowName, monitorId, callback);
		LOGGER.debug("Using settings file at " + settingsFilePath);
		LOGGER.debug("StartRunning result = " + result);
	}

	private File getSettingsFile() {
		if (settingsFile == null) {
			String settingsFilePathProperty = System.getProperty(UEYE_SETTINGS_PROPERTY);
			if (settingsFilePathProperty != null) {
				File selectedFile = new File(settingsFilePathProperty);
				if (selectedFile.exists()) {
					settingsFile = selectedFile;
				}
			}
		}
		return settingsFile;
	}

	private String getSettingsFilePath() {
		String result = NO_SETTINGS_FILE;
		if (getSettingsFile() != null ) {
			result = getSettingsFile().getAbsolutePath();
		}
		return result;
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

	public void startRecording(String videoPath) {
		int result = UEyeCapturerLibrary.StartRecording(cameraHandle, aviHandle, videoPath, 25);
		System.out.println("startRecording result: "+ result);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				// TODO should only run when recording 
				while(isRecording()) {
					LongByReference frameDropInfo = UEyeCapturerLibrary.GetFrameDropInfo(aviHandle.getValue());
					Pointer p = frameDropInfo.getPointer();
					LOGGER.debug("captured: " + p.getInt(0) + 
							" dropped: "+ p.getInt(1));
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

						// FIXME dont swallow
					}
				}
			}


		}, "FrameDropInfoThread");
		thread.start();
		setRecording(true);	
	}

	public void stopRecording() {
		int result = UEyeCapturerLibrary.StopRecording(aviHandle.getValue());
		setRecording(false);
		LOGGER.debug("StopRecording result: " + result);
	}

	/**
	 * This implementation will open the .ini settings file for the selected camera.
	 */
	public void showCaptureSettings() {
		// nothing to show here
		if (getSettingsFile() != null) {
			try {
				Desktop desktop = null;
				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
					desktop.edit(getSettingsFile());
				}
			} catch (IOException e) {
				LOGGER.error("Failed to open settings file with default editor", e);
			}
		}
	}

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

	public String getCaptureEncoderName() {
		return MJPEG_ENCODER;
	}

	public void setCaptureEncoderName(String name) {
		// do nothing, this can only be MJPEG
	}

	public List<String> getCaptureEncoderNames() {
		return Collections.singletonList(MJPEG_ENCODER);
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		firePropertyChange(VISIBLE, this.visible, this.visible = visible);
	}

	public void toFront() {
		UEyeCapturerLibrary.WndToFront(cameraHandle);
	}

	@Action(selectedProperty = VISIBLE)
	public void toggleVisibility(ActionEvent event) {
		UEyeCapturerLibrary.SetWndVisibility(cameraHandle, isVisible());
	}

	public void setMonitorId(Integer monitorId) {
		this.monitorId = monitorId;
	}

	public Integer getMonitorId() {
		return monitorId == null ? 0 : monitorId;
	}

	private void setRecording(boolean recording) {
		this.recording  = recording;
	}

	private boolean isRecording() {
		return recording;
	}

}
