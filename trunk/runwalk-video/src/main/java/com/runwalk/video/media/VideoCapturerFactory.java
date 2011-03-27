package com.runwalk.video.media;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.runwalk.video.media.dsj.DSJCapturerFactory;
import com.runwalk.video.media.ueye.UEyeCapturerFactory;

public abstract class VideoCapturerFactory {

	private static VideoCapturerFactory factory;

	protected VideoCapturerFactory() { }
	
	/**
	 * Initialize the capturer with the given name. Visible components should not be initialized until
	 * {@link IVideoCapturer#startRunning()} is called, as calls to this method can not be very expensive.
	 * 
	 * @param capturerName The name or id of the newly selected capture device
	 * @param captureEncoderName The name of the default capture encoder, null if none
	 * @return A video implementation that is ready to start running
	 */
	protected abstract IVideoCapturer initializeCapturer(String capturerName, String captureEncoderName);

	/**
	 * This method should return a list with all <b>uninitialized</b> capturer names.
	 * 
	 * @return The {@link List} with available capturers
	 */
	public abstract Collection<String> getCapturerNames();

	/**
	 * Get an implementation of a {@link VideoCapturerFactory} for the current {@link PlatformType}.
	 * @return The factory instance
	 */
	public static synchronized VideoCapturerFactory getInstance() {
		if (factory == null) {
			// at this moment capturing is only available on windows
			if (AppHelper.getPlatform() == PlatformType.WINDOWS) { 
				if (System.getProperty("native_capturer") != null) {
					factory = new UEyeCapturerFactory();
				} else {
					factory = new DSJCapturerFactory();
				}
			} else {
				factory = new DummyVideoCapturerFactory();
			}
		}
		return factory;
	}

	/** 
	 * This factory method creates a new {@link VideoCapturer} instance by showing a camera selection dialog. 
	 * At this time capturing is only available for {@link PlatformType#WINDOWS}.
	 * 
	 * @param parentComponent The {@link Window} whose focusing behavior will be inherited by the {@link CameraDialog}, can be <code>null</code>
	 * @param defaultCapturerName The name of the last chosen capturer
	 * @param defaultCaptureEncoderName The name of the default capture encoder, null if none
	 * @return The created capturer instance or null if no capturer devices were found
	 */
	public VideoCapturer createCapturer(Window parentComponent, String defaultCapturerName, final String defaultCaptureEncoderName) {
		final VideoCapturer capturer = new VideoCapturer();
		// create a dialog to let the user choose which capture device to start on which monitor
		CameraDialog dialog = new CameraDialog(parentComponent, capturer.getApplicationActionMap(), 
				capturer.getComponentId(), defaultCapturerName);
		dialog.setLocationRelativeTo(null);
		PropertyChangeListener changeListener = new PropertyChangeListener()  { 

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(CameraDialog.SELECTED_CAPTURER_NAME)) {
					// user changed capture device selection, dispose only if there was something running
					if (capturer.getVideoImpl() != null) {
						// dispose capturer from a different thread
						new Thread(new Runnable() {

							public void run() {
								capturer.getVideoImpl().dispose();
							}
							
						}, capturer.getVideoImpl().getTitle() + " Disposer").start();
					}
					// initialize the selected capturer
					String selectedCapturerName = evt.getNewValue().toString();
					try {
						IVideoCapturer capturerImpl = initializeCapturer(selectedCapturerName, defaultCaptureEncoderName);
						capturer.setVideoImpl(capturerImpl);
					} catch(RuntimeException e) {
						//TODO show appropriate error dialog here, in this case the creation will prolly return null
						Logger.getLogger(VideoCapturerFactory.class).error(e);
					}
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
			dialog.pack();
			dialog.setVisible(true);
			dialog.toFront();
			if (!dialog.isCancelled()) {
				// implementation can be null here if returned by the dummy factory
				if (capturer.getVideoImpl() != null) {
					// prepare the capturer for showing live video
					capturer.getVideoImpl().startRunning();
					// go fullscreen if screenId > 1, otherwise start in windowed mode on the first screen
					capturer.showComponent();
				}
			} else {
				// dispose chosen capturer if canceled
				capturer.dispose();
			}
		} 
		// remove the listener to avoid memory leaking
		dialog.removePropertyChangeListener(changeListener);
		// if the video implementation is null then the created capturer is useless
		return capturer.getVideoImpl() != null ? capturer : null;
	}

	/**
	 * A dummy factory that doesn't do anything.
	 * 
	 * @author Jeroen Peelaerts
	 */
	public final static class DummyVideoCapturerFactory extends VideoCapturerFactory {
		
		private DummyVideoCapturerFactory() { }

		protected IVideoCapturer initializeCapturer(String capturerName, String captureEncoderName) {
			return null;
		}

		public List<String> getCapturerNames() {
			return Collections.emptyList();
		}

		public boolean isActiveCapturer(String capturerName) {
			return false;
		}

	}

}
