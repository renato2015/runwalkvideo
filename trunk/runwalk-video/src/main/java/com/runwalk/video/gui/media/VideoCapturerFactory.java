package com.runwalk.video.gui.media;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JOptionPane;

import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.google.common.collect.Lists;
import com.runwalk.video.RunwalkVideoApp;

public abstract class VideoCapturerFactory {

	private static VideoCapturerFactory factory;
	
	/**
	 * Initialize the capturer with the given name. Visible components should not be initialized until
	 * {@link IVideoCapturer#startCapturer()} is called, as calls to this method can not be very expensive.
	 * 
	 * @param capturerName The name or id of the newly selected capture device
	 * @return A video implementation that is ready to start running
	 */
	public abstract IVideoCapturer initializeCapturer(String capturerName);
	
	/**
	 * This method should return a list with all <b>uninitialized</b> capturer names.
	 * 
	 * @return The {@link List} with available capturers
	 */
	public abstract List<String> getCapturers();

	/**
	 * This method should dispose the resources used by the given capturer after looking it up.
	 * It should check whether the given capturer is an existant and running one first.
	 * 
	 * @param capturerName The name of the capturer
	 */
	public abstract void disposeCapturer(String capturerName);

	/**
	 * Get an implementation of a {@link VideoCapturerFactory} for the current {@link PlatformType}.
	 * @return The factory
	 */
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
	
	public VideoCapturer createCapturer(final PropertyChangeListener listener) {
		return createCapturer(listener, null);
	}

	/** 
	 * This factory method creates a new {@link VideoCapturer} instance by showing a camera selection dialog. 
	 * At this time capturing is only available for {@link PlatformType#WINDOWS}.
	 * 
	 * @param listener a PropertyChangeListener to notify when internal properties change
	 * @return the created instance or null if unsupported
	 * 
	 */
	public VideoCapturer createCapturer(final PropertyChangeListener listener, String defaultCapturer) {
		final VideoCapturer capturer = new VideoCapturer(listener);
		// create a dialog to let the user choose which capture device to start on which monitor
		CameraDialog dialog = new CameraDialog(null, capturer.getApplicationActionMap(), capturer.getComponentId(), defaultCapturer);
		dialog.setLocationRelativeTo(null);
		PropertyChangeListener changeListener = new PropertyChangeListener()  { 

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(CameraDialog.SELECTED_CAPTURE_DEVICE)) {
					// user changed capture device selection. dispose if there was something running
					if (capturer.getVideoImpl() != null) {
						disposeCapturer(capturer.getVideoImpl().getTitle());
					}
					// initialize the selected capturer
					String selectedCapturer = evt.getNewValue().toString();
					IVideoCapturer capturerImpl = initializeCapturer(selectedCapturer);
					capturer.setVideoImpl(capturerImpl);
				} else if (evt.getPropertyName().equals(VideoComponent.MONITOR_ID)) {
					// user clicked a monitor button, set it on the capturer
					int monitorId = Integer.parseInt(evt.getNewValue().toString());
					capturer.setMonitorId(monitorId);
				} /*else if (evt.getPropertyName().equals(CameraDialog.CAPTURER_INITIALIZED)) {
					// prepare the capturer for showing live video
					capturer.getVideoImpl().startCapturer();
				}*/
			}
		};
		dialog.addPropertyChangeListener(changeListener);
		// populate dialog with capture devices and look for connected monitors
		if (dialog.refreshCapturers()) {
			// show the dialog on screen
			RunwalkVideoApp.getApplication().show(dialog);
			// remove the listener to avoid memory leaking
			dialog.removePropertyChangeListener(changeListener);
			// prepare the capturer for showing live video
			capturer.getVideoImpl().startCapturer();
			// go fullscreen if screenId > 1, otherwise start in windowed mode on the first screen
			capturer.showComponent();
		} else {
			JOptionPane.showMessageDialog(null, "Geen camera's gevonden..");
		}
		// if the video implementation is null then the created capturer is useless
		return capturer.getVideoImpl() == null ? null : capturer;
	}

	/**
	 * A dummy factory that doesn't do anything.
	 * 
	 * @author Jeroen Peelaerts
	 */
	public static class DummyVideoCapturerFactory extends VideoCapturerFactory {

		public IVideoCapturer initializeCapturer(String capturerName) {
			return null;
		}

		public List<String> getCapturers() {
			return Lists.newArrayList("none");
		}

		public void disposeCapturer(String capturerName) {	}

	}

}
