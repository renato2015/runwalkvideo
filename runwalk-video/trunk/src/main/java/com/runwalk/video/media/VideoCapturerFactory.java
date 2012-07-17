package com.runwalk.video.media;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import org.jdesktop.application.utils.PlatformType;

import com.runwalk.video.core.SelfContained;
import com.runwalk.video.settings.VideoCapturerFactorySettings;
import com.runwalk.video.settings.VideoCapturerSettings;

public abstract class VideoCapturerFactory<V extends VideoCapturerFactorySettings<? extends VideoCapturerSettings>> extends VideoComponentFactory<V> {
	
	public static <T extends VideoCapturerFactory<V>, V extends VideoCapturerFactorySettings<E>, E extends VideoCapturerSettings> T 
			createInstance(V videoCapturerFactorySettings, Class<T> videoCapturerFactoryClass) {
		return VideoComponentFactory.<T, V>createGenericInstance(videoCapturerFactorySettings, videoCapturerFactoryClass);
	}
	
	public static <T extends VideoCapturerFactory<VideoCapturerFactorySettings<?>>> T createInstance2(VideoCapturerFactorySettings<?> videoComponentFactorySettings, Class<? extends T> theClass) {
		T result = null;
		try {
			Class<?> factoryClass = Class.forName(videoComponentFactorySettings.getVideoComponentFactoryClassName());
			result = factoryClass.asSubclass(theClass).newInstance();
			// apply settings to the factory..
			result.loadVideoCapturerFactorySettings(videoComponentFactorySettings);
		} catch (Throwable e) {
			// any kind of error during initialization..
			// return a dummy factory if fails
			LOGGER.error("exception while instantiating factory", e);
		}
		return result;
	}

	protected VideoCapturerFactory() { 	}
	
	/**
	 * Initialize the capturer with the given name. Visible components should not be initialized until
	 * {@link IVideoCapturer#startRunning()} is called, as calls to this method can not be very expensive.
	 * 
	 * @param videoCapturerSettings Settings object used to save preferences
	 * 
	 * @return A video implementation that is ready to start running
	 */
	protected abstract IVideoCapturer initializeCapturer(VideoCapturerSettings videoCapturerSettings);

	/**
	 * This method should return a list with all <b>uninitialized</b> capturer names.
	 * 
	 * @return The {@link List} with available capturers
	 */
	public abstract Collection<String> getVideoCapturerNames();
	
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
	
	private PropertyChangeListener createDialogListener(final VideoCapturer videoCapturer, final VideoCapturerSettings videoCapturerSettings) {
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
		VideoCapturerSettings defaultVideoCapturerSettings = new VideoCapturerSettings(videoCapturerName);
		for(VideoCapturerSettings videoCapturerSettings : getVideoComponentFactorySettings().getVideoComponentSettings()) {
			if (videoCapturerName != null && videoCapturerName.equals(videoCapturerSettings.getName())) {
				defaultVideoCapturerSettings = videoCapturerSettings;
			}
		}
		return createVideoCapturer(defaultVideoCapturerSettings);
	}

	/** 
	 * This factory method creates a new {@link VideoCapturer} instance by showing a camera selection dialog. 
	 * At this time capturing is only available for {@link PlatformType#WINDOWS}. This method will always run 
	 * on the Event Dispatching Thread
	 * @param defaultCapturerName The name of the last chosen capturer
	 * @param defaultCaptureEncoderName The name of the default capture encoder, null if none
	 * 
	 * @return The created capturer instance or null if no capturer devices were found
	 */
	public VideoComponent createVideoCapturer(VideoCapturerSettings videoCapturerSettings) {
		final VideoCapturer videoCapturer = new VideoCapturer();
		Window parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
		// create a dialog to let the user choose which capture device to start on which monitor
		VideoCapturerDialog dialog = new VideoCapturerDialog(parentWindow, videoCapturer.getApplicationActionMap(), 
				videoCapturer.getComponentId(), videoCapturerSettings.getEncoderName());
		final PropertyChangeListener changeListener = createDialogListener(videoCapturer, videoCapturerSettings);
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
	 * An adapter class that takes away the burden of inheriting a quite elaborate generic signature.
	 * Can be useful when there are no specific marshallable beans used within the {@link VideoCapturerFactory}.
	 * 
	 * @author Jeroen Peelaerts
	 */
	public abstract static class Adapter extends VideoCapturerFactory<VideoCapturerFactorySettings<VideoCapturerSettings>> {	}

}
