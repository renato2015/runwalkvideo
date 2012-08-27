package com.runwalk.video.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.runwalk.video.settings.VideoCapturerFactorySettings;
import com.runwalk.video.settings.VideoComponentSettings;

public class CompositeVideoCapturerFactory extends VideoCapturerFactory.Adapter {

	private final List<VideoCapturerFactory<?>> videoCapturerFactories = new ArrayList<VideoCapturerFactory<?>>();
	
	private CompositeVideoCapturerFactory() { }
	
	/**
	 * Create a composite factory using the given {@link List} of settings. 
	 * A unchecked warning had to be suppressed because of the inability
	 * to specify parameter type information inside a class constant in java.
	 * 
	 * @param videoCapturerFactorySettingsList The list with factory setting beans
	 * @return The instantiated factory
	 */
	@SuppressWarnings("unchecked")
	public static <V extends VideoComponentSettings> CompositeVideoCapturerFactory
		createInstance(List<VideoCapturerFactorySettings<?>> videoCapturerFactorySettingsList) {
		CompositeVideoCapturerFactory result = new CompositeVideoCapturerFactory();
		Set<String> instantiatedFactoryClassNames = Sets.newHashSet();
		for (VideoCapturerFactorySettings<?> videoCapturerFactorySettings : videoCapturerFactorySettingsList) {
			String videoComponentFactoryClassName = videoCapturerFactorySettings.getVideoComponentFactoryClassName();
			if (videoComponentFactoryClassName .contains(videoComponentFactoryClassName)) {
				VideoCapturerFactory<?> videoCapturerFactory = createInstance(videoCapturerFactorySettings, VideoCapturerFactory.class); 
				result.addFactory(videoCapturerFactory);
				instantiatedFactoryClassNames.add(videoComponentFactoryClassName);
			}
		}
		return result;
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public VideoComponent createVideoCapturer(String videoCapturerName) {
		// iterate over the capturer factories, find the first one and initialize
		for(VideoCapturerFactory<?> videoCapturerFactory : videoCapturerFactories) {
			if (videoCapturerFactory.getVideoCapturerNames().contains(videoCapturerName)) {
				return videoCapturerFactory.createVideoCapturer(videoCapturerName);
			}
		}
		return null;
	}
	
	public boolean addFactory(VideoCapturerFactory<?> videoCapturerFactory) {
		return videoCapturerFactory != null ? videoCapturerFactories.add(videoCapturerFactory) : false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> getVideoCapturerNames() {
		List<String> capturerNames = new ArrayList<String>();
		for (VideoCapturerFactory<?> videoCapturerFactory : videoCapturerFactories) {
			if (capturerNames.addAll(videoCapturerFactory.getVideoCapturerNames())) {
				// TODO add some sort of separator item?? maybe later
			}
		}
		return capturerNames;
	}

}
