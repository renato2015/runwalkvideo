package com.runwalk.video.gui.media;

import com.runwalk.video.entities.Recording;


public interface IVideoPlayer extends IVideoComponent {

	public static final String FULLSCREEN = "fullscreen";
	
	public static final Object POSITION = "position";

	public static final Object PLAYING = "playing";

	public boolean mute();

	public void setPosition(int pos);

	public int getDuration();

	public void stop();

	public boolean togglePlay();

	public float getPlayRate();

	public void increaseVolume();

	public void decreaseVolume();

	public void backward();

	public void forward();

	public void nextSnapshot();

	public void previousSnapshot();

	public boolean isPlaying();

	public int makeSnapshot();

	public void loadFile(Recording recording);

	public int getPosition();

}