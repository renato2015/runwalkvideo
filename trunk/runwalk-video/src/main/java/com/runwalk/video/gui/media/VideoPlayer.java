package com.runwalk.video.gui.media;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.Timer;

import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.util.AppSettings;

public class VideoPlayer extends VideoComponent {

	public static final String POSITION = "position";

	private static int playerCount = 0;
	
	private static final float[] PLAY_RATES = AppSettings.PLAY_RATES;

	private int playRateIndex = AppSettings.getInstance().getRateIndex();
	
	private int position;
	
	private float volume;
	
	private IVideoPlayer playerImpl;

	public static VideoPlayer createInstance(PropertyChangeListener listener, Recording recording, File videoFile) throws FileNotFoundException {
		playerCount++;
		IVideoPlayer result = null;
		if (AppHelper.getPlatform() == PlatformType.WINDOWS) {
			result = new DSJPlayer();
		} else {
			result = new JMCPlayer(videoFile);
		}
		return new VideoPlayer(listener, recording, videoFile, result);
	}

	protected VideoPlayer(PropertyChangeListener listener, Recording recording, File videoFile, IVideoPlayer playerImpl) throws FileNotFoundException {
		super(listener, playerCount);
		this.playerImpl = playerImpl;
		setTimer(new Timer(25, null));
		getTimer().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (isPlaying()) {
					firePropertyChange(POSITION, position, position = getPosition());
				}
			}
		});
		loadFile(recording, videoFile);
	}

	public void loadFile(Recording recording, File videoFile) throws FileNotFoundException {
		setVideoFile(videoFile);
		setRecording(recording);
		if (getVideoImpl().loadFile(videoFile)) {
			showComponent();
		}
		// set playback rate 
		getVideoImpl().setPlayRate(getPlayRate());
		setComponentTitle(getTitle());
	}
	
	public void pause() {
		getVideoImpl().pause();
		getTimer().stop();
		setState(State.IDLE);
	}
	
	public void play() {
		getTimer().restart();
		getVideoImpl().play();
		setState(State.PLAYING);
	}

	public void stop() {
		getTimer().stop();
		setState(State.IDLE);
		getVideoImpl().stop();
	}

	/**
	 * Set the current playback position of the player. If the position is greater than the duration of the
	 * currently loaded video, then it will be set to 0.
	 * 
	 * @param pos The playback position
	 */
	public void setPosition(int pos) {
		if (pos >= getDuration()) {
			pos = 0;
		}
		getVideoImpl().setPosition(pos);
		firePropertyChange(POSITION, this.position, this.position = pos);
	}

	public boolean backward() {
		boolean result = false;
		if (result = playRateIndex > 0) {
			setPlayRateIndex(--playRateIndex);
		}
		return result;
	}

	public boolean forward() {
		boolean result = false;
		if (result = playRateIndex < PLAY_RATES.length - 1) {
			setPlayRateIndex(++playRateIndex);
		}
		return result;
	}
	
	public void pauseIfPlaying() {
		if (isPlaying()) {
			pause();
		}
	}

	public void nextSnapshot() {
		getRecording().sortKeyframes();
		for (Keyframe frame : getRecording().getKeyframes()) {
			if (frame.getPosition() > getPosition()) {
				setPosition(frame.getPosition());
				getLogger().debug("NEXT: Keyframe position " + getPosition() + " " + frame.getPosition());
				return;
			}
		}
	}

	public void previousSnapshot() {
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

	public float getPlayRate() {
		return PLAY_RATES[playRateIndex];
	}

	private void setPlayRateIndex(int rate) {
		this.playRateIndex = rate;
		AppSettings.getInstance().setRateIndex(this.playRateIndex);
		getVideoImpl().setPlayRate(getPlayRate());
	}

	public boolean mute() {
		boolean muted =  getVideoImpl().getVolume() > 0;
		if (muted) {
			volume = getVideoImpl().getVolume();
			getVideoImpl().setVolume(0f);
		} else {
			getVideoImpl().setVolume(volume);
		}
		return muted;
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
	
	public boolean isPlaying() {
		return getState() == VideoComponent.State.PLAYING;
	}

	@Override
	public String getTitle() {
		return getResourceMap().getString("windowTitle.text", getComponentId());
	}

}