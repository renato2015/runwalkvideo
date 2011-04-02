package com.runwalk.video.media;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.TreeSet;

import javax.swing.Timer;

import org.jdesktop.application.Action;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.google.common.collect.Sets;
import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.media.dsj.DSJPlayer;
import com.runwalk.video.media.jmc.JMCPlayer;

public class VideoPlayer extends VideoComponent {

	private final static TreeSet<Float> PLAY_RATES = Sets.newTreeSet(Arrays.asList(0.05f, 0.10f, 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.50f, 1.75f, 2.0f));

	public static final String POSITION = "position";

	private static final String MUTED = "muted";

	private static int playerCount = 0;

	private int position;
	
	private float volume;
	
	private IVideoPlayer playerImpl;
	
	public static VideoPlayer createInstance(String path, float playRate) {
		return createInstance(null, path, playRate);
	}

	public static VideoPlayer createInstance(Recording recording, String path, float playRate) {
		playerCount++;
		// check if the play rate is supported by the video player, if not then use the lowest one
		if (!PLAY_RATES.contains(playRate)) {
			playRate = PLAY_RATES.first();
		}
		// instantiate a suitable video player implementation for the current platform
		IVideoPlayer result = null;
		if (AppHelper.getPlatform() == PlatformType.WINDOWS) {
			result = new DSJPlayer(playRate);
		} else {
			result = new JMCPlayer(playRate);
		}
		return new VideoPlayer(recording, path, result);
	}
	
	private VideoPlayer(Recording recording, String path, IVideoPlayer playerImpl) {
		super(playerCount);
		this.playerImpl = playerImpl;
		setTimer(new Timer(25, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (isPlaying()) {
					firePropertyChange(POSITION, position, position = getPosition());
				}
			}
		}));
		loadVideo(recording, path);
	}

	public void loadVideo(Recording recording, String path) {
		setRecording(recording);
		if (getVideoImpl().loadVideo(path)) {
			// TODO fix this, hardcoded to zeroth monitor
			// showComponent(true, 0);
		}
		setComponentTitle(getTitle());
		setPosition(0);
	}
	
	public void pause() {
		setState(State.IDLE);
		getVideoImpl().pause();
		getTimer().stop();
	}
	
	public void play() {
		setState(State.PLAYING);
		// clear all previously drawn overlays
		clearOverlayImage();
		getVideoImpl().startRunning();
		getVideoImpl().play();
		getTimer().restart();
	}

	public void stop() {
		setState(State.IDLE);
		// clear all previously drawn overlays
		clearOverlayImage();
		getTimer().stop();
		// set position to 0 here and for player this instance and its 'native' implementation
		setPosition(0);
		getVideoImpl().stop();
	}
	
	private void clearOverlayImage() {
		if (isOverlayed()) {
			getVideoImpl().setOverlayImage(null, Color.white);
		}
	}

	/**
	 * Set the current playback position of the player. If the position is greater than the duration of the
	 * currently loaded video, then it will be set to 0.
	 * 
	 * @param position The playback position
	 */
	public void setPosition(int position) {
		if (position >= getDuration()) {
			position = 0;
		}
		getVideoImpl().setPosition(position);
		firePropertyChange(POSITION, this.position, this.position = position);
	}

	public float slower() {
		float playRate = getPlayRate();
		Float newPlayRate = PLAY_RATES.lower(playRate);
		if (newPlayRate != null) {
			playRate = newPlayRate;
			setPlayRate(newPlayRate);
		}
		return playRate;
	}

	public float faster() {
		Float playRate = getPlayRate();
		Float newPlayRate = PLAY_RATES.higher(playRate);
		if (newPlayRate != null) {
			playRate = newPlayRate;
			setPlayRate(newPlayRate);
		}
		return playRate;
	}
	
	public float getPlayRate() {
		return getVideoImpl().getPlayRate();
	}

	private void setPlayRate(float rate) {
		getVideoImpl().startRunning();
		getVideoImpl().setPlayRate(rate);
	}
	
	public void pauseIfPlaying() {
		if (isPlaying()) {
			pause();
		}
	}

	public void nextKeyframe() {
		getRecording().sortKeyframes();
		for (Keyframe frame : getRecording().getKeyframes()) {
			if (frame.getPosition() > getPosition()) {
				setPosition(frame.getPosition());
				getLogger().debug("NEXT: Keyframe position " + getPosition() + " " + frame.getPosition());
				return;
			}
		}
	}

	public void previousKeyframe() {
		getRecording().sortKeyframes();
		for (int i = getRecording().getKeyframeCount()-1; i >= 0; i--) {
			Keyframe frame = getRecording().getKeyframes().get(i);
			if (frame.getPosition() < getVideoImpl().getPosition()) {
				setPosition(frame.getPosition());
				getLogger().debug(getRecording().getVideoFileName() + " PREVIOUS: Keyframe position " + getVideoImpl().getPosition() + " " + frame.getPosition());
				return;
			}
		}
		setPosition(0);
	}

	public int getKeyframePosition() {
		int position = getPosition();
		getLogger().debug("Position found: " + position);
		setPosition(position);
		position = getPosition();
		getLogger().debug("Final position: " + position);
		return position;
	}
	
	public void increaseVolume() {
		getVideoImpl().setVolume(1.25f * getVideoImpl().getVolume());
	}

	public void decreaseVolume() {
		getVideoImpl().setVolume( getVideoImpl().getVolume() / 1.25f);
	}
	
	public void setMuted(boolean muted) {
		firePropertyChange(MUTED, isMuted(), muted);
	}

	@Action(enabledProperty = MUTED)
	public void mute() {
		if (isMuted()) {
			volume = getVideoImpl().getVolume();
			getVideoImpl().setVolume(0f);
		} else {
			getVideoImpl().setVolume(volume);
		}
	}
	
	public boolean isMuted() {
		return getVideoImpl().getVolume() > 0;
	}

	public int getDuration() {
		return getVideoImpl().getDuration();
	}

	public int getPosition() {
		return getVideoImpl().getPosition();
	}

	public IVideoPlayer getVideoImpl() {
		return playerImpl;
	}
	
	public void setVideoImpl(IVideoPlayer playerImpl) {
		this.playerImpl = playerImpl;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		playerCount--;
		setVideoImpl(null);
	}

	public boolean isPlaying() {
		return getState() == VideoComponent.State.PLAYING;
	}

	@Override
	public String getTitle() {
		return getResourceMap().getString("windowTitle.text", getComponentId());
	}

}