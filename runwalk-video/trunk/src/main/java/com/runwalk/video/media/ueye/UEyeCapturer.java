package com.runwalk.video.media.ueye;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;

import com.runwalk.video.core.Containable;
import com.runwalk.video.core.FullScreenSupport;
import com.runwalk.video.core.PropertyChangeSupport;
import com.runwalk.video.media.IVideoCapturer;
import com.runwalk.video.media.ueye.UEyeCapturerLibrary.OnWndShowCallback;
import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

public class UEyeCapturer implements IVideoCapturer, PropertyChangeSupport, Containable, FullScreenSupport, ComponentListener  {

	public static final String SETTINGS_FILE_PROPERTY = "ueye.settings_file";

	private static final String NATIVE_WINDOWING_PROPERTY = "ueye.native_windowing";

	private static final String NO_SETTINGS_FILE = "<default>";

	private static final String MJPEG_ENCODER = "MJPEG";
	
	private static Logger LOGGER =  Logger.getLogger(UEyeCapturer.class);
	
	private final UEyeCapturerSettings uEyeCapturerSettings;

	private IntByReference cameraHandle;

	private File settingsFile;
	
	private boolean fullScreen = false;

	/** This hook can be used by native code to call back into java */
	private Callback callback = new OnWndShowCallback() {

		public void invoke(final boolean visible) {
			firePropertyChange(VISIBLE, UEyeCapturer.this.visible, UEyeCapturer.this.visible = visible);
		}

	};

	private Integer monitorId;

	private boolean visible = false;

	private volatile boolean recording = false;

	private Component videoCanvas;

	private Frame fullScreenFrame;
	
	UEyeCapturer(UEyeCapturerSettings uEyeCapturerSettings) {
		this.uEyeCapturerSettings = uEyeCapturerSettings;
		cameraHandle = new IntByReference(uEyeCapturerSettings.getCameraId());
		int result = UEyeCapturerLibrary.InitializeCamera(cameraHandle);
		LOGGER.debug("InitializeCamera " + isSuccess(result));
		LOGGER.debug("Camera handle value = "  + cameraHandle.getValue());
	}

	private String isSuccess(int resultCode) {
		return resultCode == 0 ? "succeeded" : "failed (" + resultCode + ")";
	}
	
	public UEyeCapturerSettings getuEyeCapturerSettings() {
		return uEyeCapturerSettings;
	}

	public String getTitle() {
		return getuEyeCapturerSettings().getName();
	}

	public void dispose() {
		if (isActive()) {
			int result = UEyeCapturerLibrary.Dispose(cameraHandle);
			LOGGER.debug("Dispose " + isSuccess(result));
			// set all handles to null
			cameraHandle = null;
			callback = null;
			if (!isNativeWindowing() && fullScreenFrame != null) {
				videoCanvas.removeComponentListener(this);
				fullScreenFrame.dispose();
				fullScreenFrame = null;
				videoCanvas = null;
			}
		}
	}

	public boolean isActive() {
		return cameraHandle != null;
	}

	public Dimension getDimension() {
		Dimension result = null;
		if (!isNativeWindowing() && videoCanvas != null) {
			result = videoCanvas.getSize();
		} else {
			// TODO eventually read dimensions from settings file??
		}
		return result;
	}

	public void startRunning() {
		// create canvas and add to frame to start rendering..
		if (!isNativeWindowing() && fullScreenFrame == null) {
			fullScreenFrame = new Frame(getTitle());
			videoCanvas = new Canvas();
			videoCanvas.addComponentListener(this);
			fullScreenFrame.add(videoCanvas);
			fullScreenFrame.setBackground(Color.black);
			fullScreenFrame.setUndecorated(true);
		} else {
			IntByReference monitorId = new IntByReference(getMonitorId());
			Pointer canvasPointer = isNativeWindowing() ? Pointer.NULL : Native.getComponentPointer(videoCanvas);
			int result = UEyeCapturerLibrary.StartRunning(cameraHandle, new WString(getSettingsFilePath()), monitorId, callback, canvasPointer);
			LOGGER.debug("StartRunning " + isSuccess(result));
		}
	}

	private File getSettingsFile() {
		if (settingsFile == null) {
			String settingsFilePathProperty = getuEyeCapturerSettings().getSettingsFile();
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
	
	private int getCompressionQuality() {
		return getuEyeCapturerSettings().getCompressionQuality();
	}

	public void stopRunning() {
		if (isActive()) {
			int result = UEyeCapturerLibrary.StopRunning(cameraHandle);
			LOGGER.debug("StopRunning " + isSuccess(result));
		}
	}

	public void setOverlayImage(BufferedImage image, Color alphaColor) {
		// not implemented (yet?)
	}

	public BufferedImage getImage() {
		// TODO currently not implemented
		return null;
	}

	public void startRecording(String videoPath) {
		int result = UEyeCapturerLibrary.StartRecording(cameraHandle, videoPath, getCompressionQuality());
		LOGGER.debug("StartRecording " + isSuccess(result));

		Thread thread = new Thread(new Runnable() {
			
			public void run() {
				long startTime = System.currentTimeMillis();
				long capturedFrames = 0L, droppedFrames = 0L;
				while(isRecording()) {
					long[] frameDropInfo = {0L, 0L};
					UEyeCapturerLibrary.GetFrameDropInfo(cameraHandle, frameDropInfo);
					capturedFrames = frameDropInfo[0] & 0xFFFFFFF0L;
					droppedFrames = frameDropInfo[1] & 0xFFFFFFF0L;
					LOGGER.debug("captured: " + capturedFrames + " dropped: " + droppedFrames);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LOGGER.error(e);
					}
				}
				long recordedSeconds = (System.currentTimeMillis() - startTime) / 1000;
				BigDecimal secondsRecorded = BigDecimal.valueOf (recordedSeconds);
				BigDecimal framesCaptured = BigDecimal.valueOf(capturedFrames);
				BigDecimal fps = BigDecimal.valueOf(0);
				if (secondsRecorded.intValue() > 0) {
					fps = framesCaptured.divide(secondsRecorded, 2, RoundingMode.FLOOR);
				}
				LOGGER.debug("Average recording fps: " + fps.toPlainString());
			}


		}, "FrameDropInfoThread");
		thread.start();
		setRecording(true);	
	}

	public void stopRecording() {
		if (isRecording()) {
			int result = UEyeCapturerLibrary.StopRecording(cameraHandle);
			setRecording(false);
			LOGGER.debug("StopRecording " + isSuccess(result));
		}
	}

	/**
	 * This implementation will open the .ini settings file for the selected camera.
	 */
	public boolean showCapturerSettings() {
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
		return false;
	}

	public boolean showCameraSettings() {
		File settingsFile = getSettingsFile();
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
			if (settingsFile != null) {
				getuEyeCapturerSettings().setSettingsFile(settingsFile.getAbsolutePath());
			}
		}
		return false;
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
		if (isNativeWindowing()) {
			UEyeCapturerLibrary.SetWndVisibility(cameraHandle, isVisible());
		} else {
			if (videoCanvas != null) {
				Window windowAncestor = SwingUtilities.getWindowAncestor(videoCanvas);
				if (windowAncestor != null) {
					windowAncestor.setVisible(visible);
				}
			}
		}
		firePropertyChange(VISIBLE, this.visible, this.visible = visible);
	}

	public void toFront() {
		if (isActive()) {
			if (isNativeWindowing()) {
				UEyeCapturerLibrary.WndToFront(cameraHandle);
			} else if (fullScreenFrame != null ){
				fullScreenFrame.toFront();
			}
		}
	}

	@Action(selectedProperty = VISIBLE)
	public void toggleVisibility(ActionEvent event) {
		// check if event is originating from a component that has selected state
		if (event.getSource() instanceof AbstractButton) {
			AbstractButton source = (AbstractButton) event.getSource();
			setVisible(source.isSelected());
		}
	}
	
	@Override
	public boolean isNativeWindowing() {
		String nativeWindowing = System.getProperty(NATIVE_WINDOWING_PROPERTY);
		return Boolean.TRUE.toString().equals(nativeWindowing);
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

	public void componentMoved(ComponentEvent e) { }
	
	public void componentResized(ComponentEvent e) { }

	public void componentShown(ComponentEvent e) {
		setVisible(e.getComponent().isVisible());
	}

	public void componentHidden(ComponentEvent e) {
		setVisible(e.getComponent().isVisible());
	}
	
	public void hierarchyChanged(HierarchyEvent e) {
		// visibility of the container changed
		if (e.getChangeFlags() == HierarchyEvent.SHOWING_CHANGED) {
			setVisible(e.getChanged().isVisible());
		}
	}
	
	private GraphicsDevice getGraphicsDevice() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice foundDevice = null;
		for (GraphicsDevice device : graphicsEnvironment.getScreenDevices()) {
			if (monitorId != null && device.getIDstring().endsWith(monitorId.toString())) {
				foundDevice = device;
			}
		}
		return foundDevice;
	}

	public void enterFullScreen() {
		if (!isFullScreen() && fullScreenFrame != null && !fullScreenFrame.isVisible()) {
			setFullScreen(true);
			IntByReference monitorId = new IntByReference(getMonitorId());
			String settingsFilePath = getSettingsFilePath();
			// get rectangle for the currently selected monitor and position frame
			Rectangle bounds = getGraphicsDevice().getDefaultConfiguration().getBounds();
			fullScreenFrame.setBounds(bounds);
			fullScreenFrame.setVisible(true);
			fullScreenFrame.pack();
			// get native handle for the drawing canvas
			Pointer newPointer = Native.getComponentPointer(videoCanvas);
			LOGGER.debug("Opening camera " + getTitle());
			int result = UEyeCapturerLibrary.StartRunning(cameraHandle, new WString(settingsFilePath), monitorId, callback, newPointer);
			LOGGER.debug("Using settings file at " + settingsFilePath);
			LOGGER.debug("StartRunning " + isSuccess(result));
		}
	}
	
	public void leaveFullScreen() {
		// not implemented
	}
	
	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public boolean isFullScreen() {
		return fullScreen || isNativeWindowing();
	}

	public boolean isToggleFullScreenEnabled() {
		return false;
	}

	@Action(selectedProperty = FULL_SCREEN, enabledProperty = TOGGLE_FULL_SCREEN_ENABLED)
	public void toggleFullScreen(ActionEvent event) {
		// check if event is originating from a component that has selected state
		if (event.getSource() instanceof AbstractButton) {
			AbstractButton source = (AbstractButton) event.getSource();
			source.setSelected(true);
			// toggling is disabled for now..
		}
	}

	public Component getComponent() {
		return videoCanvas;
	}

	public boolean isResizable() {
		return false;
	}

}
