package com.runwalk.video.media.dsj;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

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
		// initialize the capturer's native resources
		videoPlayerSettings.setName("DSJ Player");
		return new DSJPlayer(videoPlayerSettings);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canHandleFile(String path) {
		// handle files that are on the default filesystem..
		FileSystem defaultFileSystem = FileSystems.getDefault();
		// TODO check if file is not in a mounted folder??
		return AppHelper.getPlatform() == PlatformType.WINDOWS;
	}
	
}
