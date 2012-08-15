package com.runwalk.video.media;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.runwalk.video.core.SelfContained;
import com.runwalk.video.settings.VideoCapturerSettings;

public abstract class VideoCapturerFactory<T extends VideoCapturerSettings> extends VideoComponentFactory<T> {

	protected VideoCapturerFactory(Class<T> capturerSettingsClass) { 	
		super(capturerSettingsClass);
	}
	
	/**
	 * This method should return a list with all <b>uninitialized</b> capturer names.
	 * 
	 * @return The {@link List} with available capturers
	 */
	public abstract Collection<String> getVideoCapturerNames();

	/**
	 * Initialize the capturer with the given name. Visible components should not be initialized until
	 * {@link IVideoCapturer#startRunning()} is called, as calls to this method can not be very expensive.
	 * 
	 * This throws an {@link UnsupportedOperationException} by default. This allows one to use the composite
	 * pattern without knowing all the explicit type parameters of its elements.
	 * 
	 * @param videoCapturerSettings Settings object used to save preferences
	 * 
	 * @return A video implementation that is ready to start running
	 */
	protected IVideoCapturer initializeCapturer(T videoCapturerSettings) {
		throw new UnsupportedOperationException("Specialized initialization routine required");
	}

	private void disposeVideoCapturer(final VideoCapturer videoCapturer) {
		if (videoCapturer.getVideoImpl() != null) {
			// dispose video capturer from a different thread
			new Thread(new Runnable() {

				public void run() {
					videoCapturer.getVideoImpl().dispose();
					videoCapturer.setVideoImpl(null);
				}

			}, videoCapturer.getVideoImpl().getTitle() + " Disposer").start();
		}
	}

	private PropertyChangeListener createDialogListener(final VideoCapturer videoCapturer, final T videoCapturerSettings) {
		return new PropertyChangeListener()  { 

			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(VideoCapturerDialog.SELECTED_VIDEO_CAPTURER_NAME)) {
					// user changed capture device selection, dispose only if there was something running
					disposeVideoCapturer(videoCapturer);
					// initialize the selected capturer
					try {
						videoCapturerSettings.setName(evt.getNewValue().toString());
						IVideoCapturer videoCapturerImpl = initializeCapturer(videoCapturerSettings);
						videoCapturer.setVideoImpl(videoCapturerImpl);
						// track object life cycle and release resources if needed
						videoCapturer.invokeAction(VideoComponent.DISPOSE_ON_EXIT_ACTION, videoCapturerImpl);
					} catch(RuntimeException e) {
						//TODO show appropriate error dialog here, in this case the creation will prolly return null
						LOGGER.error(e);
					}
				} else if (evt.getPropertyName().equals(SelfContained.MONITOR_ID)) {
					if (videoCapturer.getVideoImpl() instanceof SelfContained) {
						// user clicked a monitor button, set it on the capturer
						int monitorId = Integer.parseInt(evt.getNewValue().toString());
						videoCapturerSettings.setMonitorId(String.valueOf(monitorId));
						((SelfContained) videoCapturer.getVideoImpl()).setMonitorId(monitorId);
					}
				}
			}
		};
	}

	public VideoComponent createVideoCapturer(String videoCapturerName) {
		 T videoCapturerSettings = null;
         for (T element : getVideoComponentFactorySettings().getVideoComponentSettings()) {
        	if (element.toString().equals(element.getName())) {
            	 videoCapturerSettings = element;
             }
         }
         // if not found.. instantiate new bean
         videoCapturerSettings = videoCapturerSettings == null ? 
        		 createSettingsBean(videoCapturerName) : videoCapturerSettings;
         return createVideoCapturer(videoCapturerSettings);
	}

	/** 
	 * This method creates a new {@link VideoCapturer} instance by showing a camera selection dialog. 
	 * This method should be called on the Event Dispatching Thread.
	 * 
	 * @param defaultCapturerName The name of the last chosen capturer
	 * @param defaultCaptureEncoderName The name of the default capture encoder, null if none
	 * 
	 * @return The created capturer instance or null if no capturer devices were found
	 */
	public VideoComponent createVideoCapturer(T videoCapturerSettings) {
		final VideoCapturer videoCapturer = new VideoCapturer();
		Window parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
		// create a dialog to let the user choose which capture device to start on which monitor
		VideoCapturerDialog dialog = new VideoCapturerDialog(parentWindow, videoCapturer.getApplicationActionMap(), 
				videoCapturer.getComponentId(), videoCapturerSettings.getEncoderName());
		final PropertyChangeListener changeListener = createDialogListener(videoCapturer, videoCapturerSettings);
		dialog.addPropertyChangeListener(changeListener);
		try {
			// refresh capture devices by querying the capturer implementation for uninitialized capture devices
			Collection<String> videoCapturerNames = getVideoCapturerNames();
			if (!videoCapturerNames.isEmpty() && !dialog.isVisible()) {
				// populate dialog with capture devices and look for connected monitors
				dialog.refreshVideoCapturers(videoCapturerNames);
				// show the dialog on screen
				dialog.setVisible(true);
				dialog.toFront();
				// implementation can be null here if returned by the dummy factory
				if (!dialog.isAborted() && videoCapturer.getVideoImpl() != null) {
					// prepare the capturer for showing live video
					videoCapturer.startRunning();
				}
			} else {
				dialog.showErrorDialog();
			}
			// return if no capturers available
		} catch(Throwable throwable) {
			Logger.getLogger(getClass()).error("Error while initializing capturers", throwable);
			dialog.showErrorDialog();
		} finally {
			// remove the listener to avoid memory leaking
			dialog.removePropertyChangeListener(changeListener);
		}
		// if the video implementation is null then the created capturer is useless
		return videoCapturer.getVideoImpl() != null ? videoCapturer : null;
	}

	/**
	 * An adapter class that takes away the burden of inheriting an elaborate generic signature.
	 * Can be useful when there are no specific marshallable beans used within the {@link VideoCapturerFactory}.
	 * 
	 * @author Jeroen Peelaerts
	 */
	public abstract static class Adapter extends VideoCapturerFactory<VideoCapturerSettings> {

		public Adapter() {
			super(VideoCapturerSettings.class);
		}	
	}

}
