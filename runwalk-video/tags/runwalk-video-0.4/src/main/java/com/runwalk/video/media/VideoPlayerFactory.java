package com.runwalk.video.media;

import com.runwalk.video.media.settings.VideoPlayerSettings;

public abstract class VideoPlayerFactory<T extends VideoPlayerSettings> extends VideoComponentFactory<T> {

	public VideoPlayerFactory(Class<T> playerSettingsClass) {
		super(playerSettingsClass);
	}

	/**
	 * Return <code>true</code> if this factory is capable of creating a
	 * {@link VideoPlayer} that can handle the file specified on the given path.
	 * 
	 * @param path The path of the file to open
	 * @return <code>true</code> if a suitable videoPlayer can be instantiated
	 */
	public abstract boolean canHandleFile(String path);
	
	/**
	 * Initialize the player with the given settings.
	 * 
	 * This throws an {@link UnsupportedOperationException} by default. This allows one to use the composite
	 * pattern without knowing all the explicit type parameters of its elements.
	 * 
	 * @param videoPlayerSettings Settings object used to save preferences
	 * 
	 * @return A video implementation that is ready to start running
	 */
	protected IVideoPlayer initializePlayer(T videoPlayerSettings) {
		throw new UnsupportedOperationException("Specialized initialization routine required");
	}
	
	/**
	 * Create a VideoPlayer for the given path.
	 * 
	 * @param path The path to create a videoPlayer for
	 * @return The created videoPlayer
	 */
	public VideoPlayer createVideoPlayer(String path) {
		T videoPlayerSettings = getVideoPlayerSettings(path);
		float playRate = videoPlayerSettings.getPlayRate();
		if (!VideoPlayer.PLAY_RATES.contains(playRate)) {
			videoPlayerSettings.setPlayRate(VideoPlayer.PLAY_RATES.first());
		}
		/*videoCapturerSettings.setName(selectedVideoCapturerName);
		String monitorId = this.monitorId == -1 ? "" : Integer.toString(this.monitorId);
		videoCapturerSettings.setMonitorId(monitorId);*/
		IVideoPlayer videoPlayerImpl = initializePlayer(videoPlayerSettings);		
		return new VideoPlayer(path, videoPlayerImpl);
	}
	
	/**
	 * Get a {@link VideoPlayerSettings} bean for the given player name.
	 * 
	 * @param selectedVideoPlayerName The player's name
	 * @return The settings bean
	 */
	protected T getVideoPlayerSettings(String selectedVideoPlayerName) {
		T result = null;
		for (T videoPlayerSettings : getVideoComponentFactorySettings().getVideoComponentSettings()) {
			// just one settings bean for all players
			result = videoPlayerSettings;
		}
		// if not found.. instantiate new bean
		return result == null ? createSettingsBean(selectedVideoPlayerName) : result;
	}
	
	/**
	 * An adapter class that takes away the burden of inheriting an elaborate generic signature.
	 * Can be useful when there are no specific marshallable beans used within the {@link VideoCapturerFactory}.
	 * 
	 * @author Jeroen Peelaerts
	 */
	public abstract static class Adapter extends VideoPlayerFactory<VideoPlayerSettings> {

		public Adapter() {
			super(VideoPlayerSettings.class);
		}	
	}

}
