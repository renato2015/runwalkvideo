package com.runwalk.video.media.dsj;

import com.runwalk.video.media.IVideoPlayer;
import com.runwalk.video.media.settings.VideoPlayerSettings;

import de.humatic.dsj.DSFiltergraph;

public abstract class AbstractDSJPlayer<T extends DSFiltergraph> extends 
		DSJComponent<T, VideoPlayerSettings> implements IVideoPlayer {

	public AbstractDSJPlayer(VideoPlayerSettings videoComponentSettings) {
		super(videoComponentSettings);
	}

	public AbstractDSJPlayer() { }

	public int getDuration() {
		return getFiltergraph().getDuration();
	}

	/**
	 * Pausing a video is best accomplished by setting {@link DSFiltergraph#setRate(float)} to 0. 
	 * The {@link DSFiltergraph#pause()} command has a different purpose in DirectShow terminology.
	 */
	public void pause() {
		getFiltergraph().setRate(0);
	}

	/**
	 * Stopping a video is best accomplished by invoking {@link #pause()} and setting the playback position to 0.
	 */
	public void stop() {
		pause();
		setPosition(0);
	}

	public int getPosition() {
		return getFiltergraph().getTime();
	}

	public void setPosition(int position) {
		getFiltergraph().setTimeValue(position);
	}

	public void play() {
		setPlayRate(getVideoComponentSettings().getPlayRate());
	}

	public void setPlayRate(float rate) {
		getVideoComponentSettings().setPlayRate(rate);
		getFiltergraph().setRate(rate);
	}

	public float getPlayRate() {
		return getFiltergraph().getRate();
	}

	public float getVolume() {
		return getFiltergraph().getVolume();
	}

	public void setVolume(float volume) {
		getFiltergraph().setVolume(volume);
	}

}