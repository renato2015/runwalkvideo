package com.runwalk.video.media;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.runwalk.video.media.settings.VideoComponentFactorySettings;

public class CompositeVideoPlayerFactory extends VideoPlayerFactory.Adapter {
	
	private final List<VideoPlayerFactory<?>> videoPlayerFactories = new ArrayList<VideoPlayerFactory<?>>();
	
	private CompositeVideoPlayerFactory() { }

	/**
	 * Create a composite factory using the given {@link List} of settings. 
	 * A unchecked warning had to be suppressed because of the inability
	 * to specify parameter type information inside a class constant in java.
	 * 
	 * @param videoPlayerFactorySettingsList The list with factory setting beans
	 * @return The instantiated factory
	 */
	@SuppressWarnings("unchecked")
	public static CompositeVideoPlayerFactory createInstance(
			List<VideoComponentFactorySettings<?>> videoPlayerFactorySettingsList) {
		CompositeVideoPlayerFactory result = new CompositeVideoPlayerFactory();
		Set<String> instantiatedFactoryClassNames = Sets.newHashSet();
		for (VideoComponentFactorySettings<?> videoPlayerFactorySettings : videoPlayerFactorySettingsList) {
			String videoPlayerFactoryClassName = videoPlayerFactorySettings.getVideoComponentFactoryClassName();
			if (!instantiatedFactoryClassNames.contains(videoPlayerFactoryClassName)) {
				VideoPlayerFactory<?> videoPlayerFactory = createInstance(videoPlayerFactorySettings, VideoPlayerFactory.class); 
				result.addFactory(videoPlayerFactory);
				instantiatedFactoryClassNames.add(videoPlayerFactoryClassName);
			}
		}
		return result;
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public VideoPlayer createVideoPlayer(String path) {
		VideoPlayer result = null;
		for(VideoPlayerFactory<?> videoPlayerFactory : videoPlayerFactories) {
			if (videoPlayerFactory.canHandleFile(path)) {
				result = videoPlayerFactory.createVideoPlayer(path);
			}
		}
		return result;
	}

	public boolean addFactory(VideoPlayerFactory<?> videoPlayerFactory) {
		return videoPlayerFactory != null ? videoPlayerFactories.add(videoPlayerFactory) : false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean canHandleFile(String path) {
		boolean result = false;
		for(VideoPlayerFactory<?> videoPlayerFactory : videoPlayerFactories) {
			result |= videoPlayerFactory.canHandleFile(path);
		}
		return result;
	}
}

	