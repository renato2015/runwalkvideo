package com.runwalk.video.media;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.runwalk.video.media.settings.VideoComponentFactorySettings;

public class CompositeVideoCapturerFactory extends VideoCapturerFactory.Adapter {

	private final List<VideoCapturerFactory<?>> videoCapturerFactories = new ArrayList<VideoCapturerFactory<?>>();
	
	private CompositeVideoCapturerFactory() { }
	
	/**
	 * Create a composite factory using the given {@link List} of settings. 
	 * A unchecked warning had to be suppressed because of the inability
	 * to specify parameter type information inside a class constant in java.
	 * 
	 * @param videoComponentFactorySettingsList The list with factory setting beans
	 * @return The instantiated factory
	 */
	@SuppressWarnings("unchecked")
	public static CompositeVideoCapturerFactory createInstance(
			List<VideoComponentFactorySettings<?>> videoComponentFactorySettingsList) {
		CompositeVideoCapturerFactory result = new CompositeVideoCapturerFactory();
		Set<String> instantiatedFactoryClassNames = Sets.newHashSet();
		for (VideoComponentFactorySettings<?> videoCapturerFactorySettings : videoComponentFactorySettingsList) {
			String videoComponentFactoryClassName = videoCapturerFactorySettings.getVideoComponentFactoryClassName();
			if (!instantiatedFactoryClassNames.contains(videoComponentFactoryClassName)) {
				VideoCapturerFactory<?> videoCapturerFactory = createInstance(videoCapturerFactorySettings, VideoCapturerFactory.class); 
				result.addFactory(videoCapturerFactory);
				instantiatedFactoryClassNames.add(videoComponentFactoryClassName);
			}
		}
		return result;
	}	
	
	
	@Override
	protected PropertyChangeListener createDialogListener(VideoCapturer videoCapturer) {
		final Map<VideoCapturerFactory<?>, PropertyChangeListener> videoCapturerFactoryMapping = Maps.newHashMap();
		for (VideoCapturerFactory<?> videoCapturerFactory : videoCapturerFactories) {
			PropertyChangeListener propertyChangeListener = videoCapturerFactory.createDialogListener(videoCapturer);
			videoCapturerFactoryMapping.put(videoCapturerFactory, propertyChangeListener);
		}
		return createPropertyChangeListener(videoCapturerFactoryMapping);
	}

	private PropertyChangeListener createPropertyChangeListener(
			final Map<VideoCapturerFactory<?>, PropertyChangeListener> videoCapturerFactoryMapping) {
		return new PropertyChangeListener() {
			
			private VideoCapturerFactory<?> selectedVideoCapturerFactory;
			
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(VideoCapturerDialog.SELECTED_VIDEO_CAPTURER_NAME)) {
					String newValue = evt.getNewValue().toString();
					// should forward event to currently selected factory.?...?.?
					for(Entry<VideoCapturerFactory<?>, PropertyChangeListener> entry : videoCapturerFactoryMapping.entrySet()) {
						if (entry.getKey().getVideoCapturerNames().contains(newValue)) {
							selectedVideoCapturerFactory = entry.getKey();
						}
					}
				}
				if (selectedVideoCapturerFactory != null) {
					PropertyChangeListener propertyChangeListener = videoCapturerFactoryMapping.get(selectedVideoCapturerFactory);
					propertyChangeListener.propertyChange(evt);
				}
			}
			
		};
	}

	/**
	 * This method overrides the original implementation because
	 * the composite will have a <code>null</code> return value for the
	 * {@link #getVideoComponentFactorySettings()} method.
	 * 
	 * The default capturer name will therefore be the default one of the
	 * first registered factory in the list contained by the composite.
	 * 
	 */
	@Override
	public VideoComponent createVideoCapturer() {
		VideoComponent result = null;
		if (!videoCapturerFactories.isEmpty()) {
			VideoCapturerFactory<?> firstVideoCapturerFactory = videoCapturerFactories.get(0);
			VideoComponentFactorySettings<?> videoComponentFactorySettings = 
					firstVideoCapturerFactory.getVideoComponentFactorySettings();
			String defaultVideoComponentName = videoComponentFactorySettings.getDefaultVideoComponentName();
			result = createVideoCapturer(defaultVideoComponentName);
		}
		return result;
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
			capturerNames.addAll(videoCapturerFactory.getVideoCapturerNames());
		}
		return capturerNames;
	}

}
