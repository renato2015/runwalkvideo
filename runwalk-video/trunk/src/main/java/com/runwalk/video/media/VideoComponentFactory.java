package com.runwalk.video.media;

import java.util.List;

import org.apache.log4j.Logger;

import com.runwalk.video.media.settings.VideoComponentFactorySettings;
import com.runwalk.video.media.settings.VideoComponentSettings;

public class VideoComponentFactory<T extends VideoComponentSettings> {
	
	protected final static Logger LOGGER = Logger.getLogger(VideoCapturerFactory.class);
	
	private final Class<T> videoComponentSettingsClass;
	
	private VideoComponentFactorySettings<T> videoComponentFactorySettings;
	
	public VideoComponentFactory(Class<T> videoComponentSettingsClass) {
		this.videoComponentSettingsClass = videoComponentSettingsClass;
	}

	/**
	 * Create a factory with explicitly type information. Use this method whenever possible as it typesafer in comparison to
	 * its overloaded one argument version.
	 * 
	 * @param videoComponentFactorySettings The factory settings bean
	 * @param theClass Type information of the factory to be created
	 * @return The instantiated factory
	 */
	public static <T extends VideoComponentFactory<V>, V extends VideoComponentSettings> T 
			createInstance(VideoComponentFactorySettings<V> videoComponentFactorySettings, Class<? extends T> theClass) {
		T result = null;
		try {
			Class<?> factoryClass = Class.forName(videoComponentFactorySettings.getVideoComponentFactoryClassName());
			result = factoryClass.asSubclass(theClass).newInstance();
			// apply settings to the factory..
			result.setVideoCapturerFactorySettings(videoComponentFactorySettings);
		} catch (Throwable e) {
			// any kind of error during initialization..
			// return a dummy factory if fails
			LOGGER.error("Exception while instantiating factory", e);
		}
		return result;
	}
	
	/**
	 * Create a generic settings bean and add it to the list of settings 
	 * owned by the corresponding {@link VideoComponentFactorySettings}.
	 * 
	 * @param videoComponentName The name of the component to create
	 * @return The created component
	 */
	protected T createSettingsBean(String videoComponentName) {
		try {
			T result = getVideoComponentSettingsClass().newInstance();
			result.setName(videoComponentName);
			// add the settings bean to the factory's settings
			videoComponentFactorySettings.addVideoComponentSettings(result);
			return result;
		} catch (InstantiationException e) {
			LOGGER.error("Exception while instantiating settings bean", e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Exception while instantiating settings bean", e);
		}
		return null;
	}
	
	public void setVideoCapturerFactorySettings(VideoComponentFactorySettings<T> videoCapturerFactorySettings) {
		this.videoComponentFactorySettings = videoCapturerFactorySettings;
	}

	public VideoComponentFactorySettings<T> getVideoComponentFactorySettings() {
		return videoComponentFactorySettings;
	}
	
	public Class<T> getVideoComponentSettingsClass() {
		return videoComponentSettingsClass;
	}
	
	/**
	 * This methods will find the associated monitor id for the given component name.
	 * @param componentName The component name to find the monitor id for
 	 * @return The monitor id, or <code>null</code> if undefined
	 */
	public Integer getMonitorIdForComponent(String componentName) {
		List<T> videoComponentSettingsList = getVideoComponentFactorySettings().getVideoComponentSettings();
		for (T videoComponentSettings : videoComponentSettingsList) {
			if (videoComponentSettings.getName() != null && 
					videoComponentSettings.getName().equals(componentName)) {
				String monitorId = videoComponentSettings.getMonitorId();
				if (monitorId != null && monitorId.length() > 0) {
					return Integer.parseInt(monitorId);
				}
			}
		}
		return null;
	}

}
