package com.runwalk.video.media;

import java.util.Collections;
import java.util.List;

import com.runwalk.video.settings.VideoCapturerSettings;

/**
 * A dummy factory that doesn't do anything.
 * 
 * @author Jeroen Peelaerts
 */
public final class DummyVideoCapturerFactory extends VideoCapturerFactory {
	
	private DummyVideoCapturerFactory() { }

	protected IVideoCapturer initializeCapturer(VideoCapturerSettings videoCapturerSettings) {
		return null;
	}

	public List<String> getVideoCapturerNames() {
		return Collections.emptyList();
	}

	public boolean isActiveCapturer(String capturerName) {
		return false;
	}

}