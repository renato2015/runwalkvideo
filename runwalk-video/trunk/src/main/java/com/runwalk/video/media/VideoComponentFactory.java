package com.runwalk.video.media;

import org.apache.log4j.Logger;

import com.runwalk.video.settings.VideoComponentFactorySettings;

public class VideoComponentFactory<V extends VideoComponentFactorySettings<?>> {
	
	protected final static Logger LOGGER = Logger.getLogger(VideoCapturerFactory.class);
	
	private V videoComponentFactorySettings;
	
	/**
	 * Create a factory with explicitly type information. Use this method whenever possible as it typesafer in comparison to
	 * its overloaded one argument version.
	 * 
	 * @param videoComponentFactorySettings The factory settings bean
	 * @param theClass Type information of the factory to be created
	 * @return The instantiated factory
	 */
	public static <T extends VideoComponentFactory<V>, V extends VideoComponentFactorySettings<?>> T 
			createInstance(V videoComponentFactorySettings, Class<? extends T> theClass) {
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
	
	/**
	 * Create a factory without explicit type information. This method is not type safe. Use only when 
	 * there is no type information available at compile time.
	 * 
	 * @param videoComponentFactorySettings The factory settings bean
	 * @return The instantiated factory
	 */
	@SuppressWarnings("unchecked")
	public static <T extends VideoComponentFactory<V>, V extends VideoComponentFactorySettings<?>> T 
			createInstance(V videoComponentFactorySettings) {
		T result = null;
		try {
			Class<?> factoryClass = Class.forName(videoComponentFactorySettings.getVideoComponentFactoryClassName());
			result = (T) factoryClass.newInstance();
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
