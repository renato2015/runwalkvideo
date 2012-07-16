package com.runwalk.video.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.runwalk.video.settings.CompositeVideoCapturerFactorySettings;
import com.runwalk.video.settings.VideoCapturerFactorySettings;
import com.runwalk.video.settings.VideoCapturerSettings;

public class CompositeVideoCapturerFactory extends VideoCapturerFactory {

	private final List<VideoCapturerFactory> videoCapturerFactories = new ArrayList<VideoCapturerFactory>();
	
	public CompositeVideoCapturerFactory() { }
	
	@Override
	public void loadVideoCapturerFactorySettings(VideoCapturerFactorySettings compositeVideoCapturerFactorySettings) {
		// instantiate other factories here
		if (compositeVideoCapturerFactorySettings instanceof CompositeVideoCapturerFactorySettings) {
			for(VideoCapturerFactorySettings videoCapturerFactorySettings : 
				((CompositeVideoCapturerFactorySettings) compositeVideoCapturerFactorySettings).getVideoCapturerFactorySettings()) {
				videoCapturerFactories.add(createInstance(videoCapturerFactorySettings));
			}
		}
		super.loadVideoCapturerFactorySettings(compositeVideoCapturerFactorySettings);
	}

	@Override
	protected IVideoCapturer initializeCapturer(VideoCapturerSettings videoCapturerSettings) {
		// iterate over the capturer factories, find the first one and initialize
		for(VideoCapturerFactory videoCapturerFactory : videoCapturerFactories) {
			if (videoCapturerFactory.getVideoCapturerNames().contains(videoCapturerSettings.getName())) {
				return videoCapturerFactory.initializeCapturer(videoCapturerSettings);
			}
		}
		return null;
	}

	@Override
	public Collection<String> getVideoCapturerNames() {
		List<String> capturerNames = new ArrayList<String>();
		for (VideoCapturerFactory videoCapturerFactory : videoCapturerFactories) {
			if (capturerNames.addAll(videoCapturerFactory.getVideoCapturerNames())) {
				// TODO add some sort of separator item?? maybe later
			}
		}
		return capturerNames;
	}

}
