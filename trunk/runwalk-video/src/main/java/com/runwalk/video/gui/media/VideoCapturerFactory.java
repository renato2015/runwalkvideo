package com.runwalk.video.gui.media;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.runwalk.video.RunwalkVideoApp;

public abstract class VideoCapturerFactory {

	private static VideoCapturerFactory factory;
	
	private String selectedCapturer;

	public static synchronized VideoCapturerFactory getInstance() {
		if (factory == null) {
			// at this moment capturing is only available on windows
			if (AppHelper.getPlatform() == PlatformType.WINDOWS) { 
				factory = new DSJCapturerFactory();
			} else {
				factory = new DummyVideoCapturerFactory();
			}
		}
		return factory;
	}

	/** 
	 * This factory method creates a new {@link VideoCapturer} instance by showing a camera selection dialog. 
	 * At this time capturing is only available for {@link PlatformType#WINDOWS}. Calling this method
	 * on any other platform will return null without showing the selection dialog.
	 * 
	 * @param listener a PropertyChangeListener to notify when internal properties change
	 * @return the created instance or null if unsupported
	 * 
	 */
	public VideoCapturer createCapturer(final PropertyChangeListener listener) {
		final VideoCapturer capturer = new VideoCapturer(listener);
		// create a dialog to let the user choose which capture device to start on which monitor
		CameraDialog dialog = new CameraDialog(null, capturer.getApplicationActionMap(), capturer.getComponentId());
		dialog.setLocationRelativeTo(null);
		PropertyChangeListener changeListener = new PropertyChangeListener()  { 

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(CameraDialog.SELECTED_CAPTURE_DEVICE)) {
					// user changed capture device selection. dispose if there was something running
					disposeCapturer(selectedCapturer);
					selectedCapturer = evt.getNewValue().toString();
				} else if (evt.getPropertyName().equals(VideoComponent.MONITOR_ID)) {
					// user clicked a monitor button
					int monitorId = Integer.parseInt(evt.getNewValue().toString());
					capturer.setMonitorId(monitorId);
				} else if (evt.getPropertyName().equals(CameraDialog.CAPTURER_INITIALIZED)) {
					IVideoCapturer capturerImpl = initializeCapturer(selectedCapturer);
					capturer.setVideoImpl(capturerImpl);
				}
			}
		};
		dialog.addPropertyChangeListener(changeListener);
		//populate dialog with capture devices and look for connected monitors
		dialog.refreshCaptureDevices();
		// show the dialog on screen
		RunwalkVideoApp.getApplication().show(dialog);
		// remove the listener to avoid memory leaking
		dialog.removePropertyChangeListener(changeListener);
		// go fullscreen if screenId > 1, otherwise start in windowed mode on the first screen
		capturer.showComponent();
		// if the video implementation is null then the created capturer is useless
		return capturer.getVideoImpl() == null ? null : capturer;
	}

	/**
	 * Initialize the capturer and bring it to a state in which the {@link Component} returned by {@link #getComponent()} 
	 * or the {@link java.awt.Window} returned by {@link #getFullscreenFrame()} are running and ready to be added to a swing container.
	 * 
	 * @param capturerName The name or id of the newly selected capture device
	 */
	public abstract IVideoCapturer initializeCapturer(String capturerName);

	public abstract String[] getCaptureDevices();

	public abstract void disposeCapturer(String capturerName);

	/**
	 * A dummy factory that doesn't do anything.
	 * 
	 * @author Jeroen Peelaerts
	 */
	public static class DummyVideoCapturerFactory extends VideoCapturerFactory {

		public IVideoCapturer initializeCapturer(String capturerName) {
			return null;
		}

		public String[] getCaptureDevices() {
			return new String[] {"none"};
		}

		public void disposeCapturer(String capturerName) {
		}

	}

}
