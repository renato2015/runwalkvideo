package com.runwalk.video.media;

import org.apache.log4j.Logger;

import com.runwalk.video.settings.VideoComponentFactorySettings;

public class VideoComponentFactory<V extends VideoComponentFactorySettings<?>> {
	
	protected final static Logger LOGGER = Logger.getLogger(VideoCapturerFactory.class);
	
	private V videoComponentFactorySettings;
	
	public static <T extends VideoComponentFactory<V>, V extends VideoComponentFactorySettings<?>> T 
			createGenericInstance(V videoComponentFactorySettings, Class<? extends T> theClass) {
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
	
	public void loadVideoCapturerFactorySettings(V videoCapturerFactorySettings) {
		this.videoComponentFactorySettings = videoCapturerFactorySettings;
	}

	public V getVideoComponentFactorySettings() {
		return videoComponentFactorySettings;
	}
	
}
