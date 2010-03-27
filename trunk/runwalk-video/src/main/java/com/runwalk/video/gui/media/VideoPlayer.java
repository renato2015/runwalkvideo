package com.runwalk.video.gui.media;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;

import javax.swing.Timer;

import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.util.AppSettings;

public class VideoPlayer extends VideoComponent {

	public static final String POSITION = "position";

	public static final String PLAYING = "playing";

	private static int playerCount = 0;
	private int playerId;
	private static final float[] PLAY_RATES = AppSettings.PLAY_RATES;
	private int playRateIndex = AppSettings.getInstance().getRateIndex();
	private int position;
	private float volume;
	private boolean playing;
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
			getFullscreenFrame().addWindowListener(new WindowAdapter() {

				public void windowGainedFocus(WindowEvent e) {
					if (!isPlaying()) {
						setControlsEnabled(false);
					}
				}

				public void windowActivated(WindowEvent e) {
					if (!isPlaying()) {
						setControlsEnabled(true);
					}
				}

				public void windowDeactivated(WindowEvent e) {
					setControlsEnabled(false);
				}

/*				public void windowClosed(WindowEvent e) {
					setControlsEnabled(false);
				}*/

			});
		}
		//getVideoImpl().setPlayRate(getPlayRate());
		setComponentTitle(getTitle());
	}

	public boolean togglePlay() {
		if (isPlaying()) {
			getVideoImpl().pause();
			getTimer().stop();
			setPlaying(false);
		} else {
			getTimer().restart();
			getVideoImpl().play();
			setPlaying(true);
		}
		return isPlaying();
	}

	public void stop() {
		getTimer().stop();
		setPlaying(false);
		getVideoImpl().stop();
	}

	protected void setPlaying(boolean playing) {
		firePropertyChange(PLAYING, this.playing, this.playing = playing);
	}

	public void setPosition(int pos) {
		getVideoImpl().setPosition(pos);
		firePropertyChange(POSITION, this.position, this.position = pos);
	}

	public boolean isPlaying() {
		return playing;
	}

	public void backward() {
		if (playRateIndex > 0) {
			setPlayRateIndex(--playRateIndex);
		} else {
			getVideoImpl().pause();
		}
	}

	public void forward() {
		if (isPlaying() && playRateIndex < PLAY_RATES.length - 1) {
			setPlayRateIndex(++playRateIndex);
		} else if (!isPlaying()) {
			togglePlay();
		}
	}

	public void nextSnapshot() {
		if (isPlaying()) {
			togglePlay();
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
		if (getVideoImpl().getVolume() > 0) {
			volume = getVideoImpl().getVolume();
			getVideoImpl().setVolume(0f);
			return true;
		} else {
			getVideoImpl().setVolume(volume);
			return false;
		}
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

	@Override
	public String getTitle() {
		return getResourceMap().getString("windowTitle.text", playerId);
	}
	
	

}