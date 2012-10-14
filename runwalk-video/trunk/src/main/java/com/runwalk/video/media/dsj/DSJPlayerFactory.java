package com.runwalk.video.media.dsj;

import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.runwalk.video.media.IVideoPlayer;
import com.runwalk.video.media.VideoPlayerFactory;
import com.runwalk.video.media.settings.VideoPlayerSettings;

public class DSJPlayerFactory extends VideoPlayerFactory.Adapter {
	
	public DSJPlayerFactory() {	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IVideoPlayer initializePlayer(VideoPlayerSettings videoPlayerSettings) {
		IVideoPlayer result = null;
		videoPlayerSettings.setName("DSJ Player");
		if (videoPlayerSettings.isAsynchronous()) {
			result = new DSJAsyncPlayer(videoPlayerSettings);
		} else {
			result = new DSJPlayer(videoPlayerSettings);
		}
		// initialize the capturer's native resources
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canHandleFile(String path) {
		return AppHelper.getPlatform() == PlatformType.WINDOWS;
	}
	
}
