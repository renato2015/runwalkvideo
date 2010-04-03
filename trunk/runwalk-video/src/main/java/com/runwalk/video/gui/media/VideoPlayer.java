package com.runwalk.video.gui.media;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;

import javax.swing.Timer;

import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.util.AppSettings;

public class VideoPlayer extends VideoComponent {

	public static final String POSITION = "position";

	private static int playerCount = 0;
	private int playerId;
	private static final float[] PLAY_RATES = AppSettings.PLAY_RATES;
	private int playRateIndex = AppSettings.getInstance().getRateIndex();
	private int position;
	private float volume;
	private IVideoPlayer playerImpl;

	public static VideoPlayer createInstance(PropertyChangeListener listener, Recording recording) throws FileNotFoundException {
		IVideoPlayer result = null;
		if (System.getProperty("os.name").contains("Windows")) {
			result = new DSJPlayer();
		} else {
			result = new JMCPlayer(recording.getVideoFile());
		}
		return new VideoPlayer(listener, recording, result);
	}

	protected VideoPlayer(PropertyChangeListener listener, Recording recording, IVideoPlayer playerImpl) throws FileNotFoundException {
		super(listener);
		this.playerImpl = playerImpl;
		playerCount++;
		this.playerId = playerCount;
		setTimer(new Timer(25, null));
		getTimer().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (isPlaying()) {
					firePropertyChange(POSITION, position, position = getVideoImpl().getPosition());
				}
			}
		});
		loadFile(recording);
		getVideoImpl().setPlayRate(getPlayRate());
	}

	public void loadFile(Recording recording) throws FileNotFoundException {
		setRecording(recording);
		if (getVideoImpl().loadFile(recording.getVideoFile())) {
			//TODO the window state should be respected here...
			//Add the shit here...
			setFullscreen(true);
			reAttachAppWindowWrapperListeners();
		}
		//getVideoImpl().setPlayRate(getPlayRate());
		setComponentTitle(getTitle());
	}

	public boolean togglePlay() {
		if (isPlaying()) {
			pause();
		} else {
			play();
		}
		return isPlaying();
	}
	
	public void pause() {
		getVideoImpl().pause();
		getTimer().stop();
		setState(VideoComponent.State.IDLE);
	}
	
	public void play() {
		getTimer().restart();
		getVideoImpl().play();
		setState(VideoComponent.State.PLAYING);
	}

	public void stop() {
		getTimer().stop();
		setState(VideoComponent.State.IDLE);
		getVideoImpl().stop();
	}

	public void setPosition(int pos) {
		getVideoImpl().setPosition(pos);
		firePropertyChange(POSITION, this.position, this.position = pos);
	}

	public void backward() {
		if (playRateIndex > 0) {
			setPlayRateIndex(--playRateIndex);
		} else {
			pause();
		}
	}

	public void forward() {
		if (isPlaying() && playRateIndex < PLAY_RATES.length - 1) {
			setPlayRateIndex(++playRateIndex);
		} else if (!isPlaying()) {
			play();
		}
	}

	public void nextSnapshot() {
		if (isPlaying()) {
			pause();
		}
		getRecording().sortKeyframes();
		for (Keyframe frame : getRecording().getKeyframes()) {
			if (frame.getPosition() > getVideoImpl().getPosition()) {
				setPosition(frame.getPosition());
				getLogger().debug("NEXT: Keyframe position " + getVideoImpl().getPosition() + " " + frame.getPosition());
				return;
			}
		}
	}

	public void previousSnapshot() {
		if (isPlaying()) {
			togglePlay();
		}
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

	public int makeSnapshot() {
		if (isPlaying()) {
			togglePlay();
		}
		int position = getVideoImpl().getPosition();
		getLogger().debug("Position found :" + position);
		setPosition(position);
		position = getVideoImpl().getPosition();
		getLogger().debug("Final position :" + position);
		getRecording().addKeyframe(position);
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
		return getResourceMap().getString("windowTitle.text", playerId);
	}

}