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

import com.runwalk.video.core.SelfContained;
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
	public abstract Collection<String> getVideoCapturerNames();
	
	/**
	 * Get an implementation of a {@link VideoCapturerFactory} for the current {@link PlatformType}.
	 * @return The factory instance
	 */
	public static synchronized VideoCapturerFactory getInstance() {
		if (factory == null) {
			// at this moment capturing is only available on windows
			if (AppHelper.getPlatform() == PlatformType.WINDOWS) { 
				// load factories using reflection??
				String nativeCapturer = System.getProperty("ueye.enable");
				if (Boolean.TRUE.toString().equals(nativeCapturer)) {
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
	 * At this time capturing is only available for {@link PlatformType#WINDOWS}. This method will always run 
	 * on the Event Dispatching Thread
	 * 
	 * @param parentComponent The {@link Window} whose focusing behavior will be inherited by the {@link VideoCapturerDialog}, can be <code>null</code>
	 * @param defaultCapturerName The name of the last chosen capturer
	 * @param defaultCaptureEncoderName The name of the default capture encoder, null if none
	 * @return The created capturer instance or null if no capturer devices were found
	 */
	public VideoComponent createCapturer(Window parentComponent, String defaultCapturerName, final String defaultCaptureEncoderName) {
		final VideoCapturer videoCapturer = new VideoCapturer();
		// create a dialog to let the user choose which capture device to start on which monitor
		VideoCapturerDialog dialog = new VideoCapturerDialog(parentComponent, videoCapturer.getApplicationActionMap(), 
				videoCapturer.getComponentId(), defaultCapturerName);
		PropertyChangeListener changeListener = new PropertyChangeListener()  { 

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(VideoCapturerDialog.SELECTED_VIDEO_CAPTURER_NAME)) {
					// user changed capture device selection, dispose only if there was something running
					if (videoCapturer.getVideoImpl() != null) {
						// dispose capturer from a different thread
						new Thread(new Runnable() {

							public void run() {
								videoCapturer.getVideoImpl().dispose();
								videoCapturer.setVideoImpl(null);
							}
							
						}, videoCapturer.getVideoImpl().getTitle() + " Disposer").start();
					}
					// initialize the selected capturer
					String selectedCapturerName = evt.getNewValue().toString();
					try {
						IVideoCapturer videoCapturerImpl = initializeCapturer(selectedCapturerName, defaultCaptureEncoderName);
						videoCapturer.setVideoImpl(videoCapturerImpl);
						// track object life cycle and release resources if needed
						videoCapturer.invokeAction(VideoComponent.DISPOSE_ON_EXIT_ACTION, videoCapturerImpl);
					} catch(RuntimeException e) {
						//TODO show appropriate error dialog here, in this case the creation will prolly return null
						Logger.getLogger(VideoCapturerFactory.class).error(e);
					}
				} else if (evt.getPropertyName().equals(SelfContained.MONITOR_ID)) {
					if (videoCapturer.getVideoImpl() instanceof SelfContained) {
						// user clicked a monitor button, set it on the capturer
						int monitorId = Integer.parseInt(evt.getNewValue().toString());
						((SelfContained) videoCapturer.getVideoImpl()).setMonitorId(monitorId);
					}
				}
			}
		};
		dialog.addPropertyChangeListener(changeListener);
		// populate dialog with capture devices and look for connected monitors
		if (dialog.refreshVideoCapturers()) {
			// show the dialog on screen
			dialog.setVisible(true);
			dialog.toFront();
			// implementation can be null here if returned by the dummy factory
			if (!dialog.isAborted() && videoCapturer.getVideoImpl() != null) {
				// prepare the capturer for showing live video
				videoCapturer.startRunning();
			}
		} 
		// remove the listener to avoid memory leaking
		dialog.removePropertyChangeListener(changeListener);
		// if the video implementation is null then the created capturer is useless
		return videoCapturer.getVideoImpl() != null ? videoCapturer : null;
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

		public List<String> getVideoCapturerNames() {
			return Collections.emptyList();
		}

		public boolean isActiveCapturer(String capturerName) {
			return false;
		}

	}

}
