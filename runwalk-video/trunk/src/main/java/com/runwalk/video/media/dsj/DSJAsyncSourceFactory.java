package com.runwalk.video.media.dsj;

import com.runwalk.video.media.IVideoPlayer;
import com.runwalk.video.media.VideoPlayerFactory;
import com.runwalk.video.media.settings.VideoPlayerSettings;

public class DSJAsyncSourceFactory extends VideoPlayerFactory.Adapter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IVideoPlayer initializePlayer(VideoPlayerSettings videoPlayerSettings) {
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canHandleFile(String path) {
		// TODO should check if the file is on a network drive or not??
		return false;
	}
	
}
