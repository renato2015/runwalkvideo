package com.runwalk.video.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdesktop.application.utils.PlatformType;

public class CompositeVideoCapturerFactory extends VideoCapturerFactory {

	private final List<VideoCapturerFactory> videoCapturerFactories;
	
	public CompositeVideoCapturerFactory(List<String> classNames) {
		videoCapturerFactories = new ArrayList<VideoCapturerFactory>();
		// public constructor.. load factories using reflection
		for (String className : classNames) {
			VideoCapturerFactory factory = loadFactory(className);
			if (factory != null) {
				videoCapturerFactories.add(factory);
			}
		}
	}
	
	private VideoCapturerFactory loadFactory(String className) {
		try {
			Class<? extends VideoCapturerFactory> theClass = Class.forName(className).asSubclass(VideoCapturerFactory.class);
			return theClass.newInstance();
		} catch (ClassNotFoundException e) {
			Logger.getLogger(CompositeVideoCapturerFactory.class).error("Class not found for factory " + className, e);
		} catch (IllegalAccessException e) {
			Logger.getLogger(CompositeVideoCapturerFactory.class).error("Could not instantiate factory " + className, e);
		} catch (InstantiationException e) {
			Logger.getLogger(CompositeVideoCapturerFactory.class).error("Could not instantiate factory " + className, e);
		}
		return null;
	}

	@Override
	protected IVideoCapturer initializeCapturer(String capturerName, String captureEncoderName) {
		// iterate over the capturer factories, find the first one and initialize
		for(VideoCapturerFactory videoCapturerFactory : videoCapturerFactories) {
			if (videoCapturerFactory.getCapturerNames().contains(capturerName)) {
				return videoCapturerFactory.initializeCapturer(capturerName, captureEncoderName);
			}
		}
		return null;
	}

	@Override
	public Collection<String> getCapturerNames() {
		List<String> capturerNames = new ArrayList<String>();
		for (VideoCapturerFactory videoCapturerFactory : videoCapturerFactories) {
			if (capturerNames.addAll(videoCapturerFactory.getCapturerNames())) {
				// TODO add some sort of separator item?? maybe later
			}
		}
		return capturerNames;
	}

	protected boolean isPlatformSupported(PlatformType platformType) {
		return true;
	}

}
