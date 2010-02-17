package com.runwalk.video.gui.media;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.jdesktop.application.Action;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.util.AppSettings;

import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSMovie;


public class DSJPlayer extends DSJComponent<DSMovie> implements IVideoPlayer {

	private static int playerCount = 0;

	private int playerId;

	public static final String POSITION = "position";

	public static final String PLAYING = "playing";

	private final static float[] PLAY_RATES = AppSettings.PLAY_RATES;

	private boolean playing = false;

	private int playRateIndex = AppSettings.getInstance().getRateIndex();

	private boolean customFramerateEnabled = false;

	private float framerate;

	private int position, timePlaying;

	private float volume;

	public DSJPlayer(PropertyChangeListener listener, Recording recording) {
		super(listener);
		playerCount++;
		this.playerId = playerCount;
		setTimer(new Timer(50, null));
		getTimer().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (isPlaying()) {
					if (getPosition() == 0) {
						stop();
					}
					firePropertyChange(POSITION, timePlaying, timePlaying = getPosition());
				}
			}
		});
		loadFile(recording);
	}

	public void loadFile(Recording recording) {
		setRecording(recording);
		String path = null;
		try {
			path = recording.getVideoFilePath();
			if (getFiltergraph() == null) {
				initFiltergraph(path);
			} else {
				getFiltergraph().loadFile(path , 0);
			}
		} catch(DSJException e) {
			/*JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
					"Er heeft zich een fout voorgedaan bij het openen van een filmpje.\n" +
					"Probeer het gerust nog eens opnieuw.",
					"Fout bij openen filmpje",
					JOptionPane.ERROR_MESSAGE);*/
			getLogger().error("Movie initialization failed.", e);
			dispose(false);
			initFiltergraph(path);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
					"Het bestand dat u probeerde te openen kon niet worden gevonden",
					"Fout bij openen filmpje",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void initFiltergraph(String path) {
		getLogger().debug("Movie path opened: " + path);
		int flags = DSFiltergraph.DD7 | DSMovie.INIT_PAUSED;
		if (customFramerateEnabled) {
			flags = flags | DSMovie.INIT_EDITABLE;
		}
		setFiltergraph(new DSMovie(path, flags, getPropertyChangeListeners()[0]));
		if (customFramerateEnabled) {
			getFiltergraph().setMasterFrameRate(framerate);
		}
		getFiltergraph().lockAspectRatio(true);
		getFiltergraph().setRecueOnStop(true);
	}

	@Action
	public void setCustomFramerate() {
		try {
			if (getFiltergraph() != null && getFiltergraph().getActive()) {
				customFramerateEnabled = true;
				String prefferredRate = JOptionPane.showInputDialog(RunwalkVideoApp.getApplication().getMainFrame(), 
						"Geef een framerate in..", "Set framerate op capture device", JOptionPane.PLAIN_MESSAGE);
				framerate = Float.parseFloat(prefferredRate);
				getFiltergraph().setMasterFrameRate(framerate);
			} else {
				JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(), 
				"Geen actieve filtergraph gevonden..");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(), 
					"Framerate was ongeldig: " + e.getMessage());
		}
	}

	public int getDuration(){
		return getFiltergraph().getDuration();
	}

	public boolean togglePlay() {
		if (isPlaying()) {
			pause();
		} else {
			getTimer().restart();
			getFiltergraph().play();
			getFiltergraph().setRate(getPlayRate());
			setPlaying(true);
		}
		return isPlaying();
	}

	private void suspendPlayer() {
		getTimer().stop();
		setPlaying(false);
	}

	public void pause() {
		suspendPlayer();
		getFiltergraph().pause();
	}

	public void stop() {
		suspendPlayer();
		getFiltergraph().stop();
		getFiltergraph().setTimeValue(0);
	}

	public void forward() {
		if (isPlaying() && playRateIndex < PLAY_RATES.length - 1) {
			setPlayRateIndex(++playRateIndex);
		} else if (!isPlaying()) {
			togglePlay();
		}
	}

	public void backward() {
		if (playRateIndex > 0) {
			setPlayRateIndex(--playRateIndex);
		} else {
			pause();
		}
	}

	public void nextSnapshot() {
		if (isPlaying()) {
			togglePlay();
		}
		getRecording().sortKeyframes();
		for (Keyframe frame : getRecording().getKeyframes()) {
			if (frame.getPosition() > getPosition()) {
				setPosition(frame.getPosition());
				getLogger().debug("NEXT: Keyframe position " + getPosition() + " " + frame.getPosition());
				break;
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
			if (frame.getPosition() < getPosition()) {
				setPosition(frame.getPosition());
				getLogger().debug(getRecording().getVideoFileName() + " PREVIOUS: Keyframe position " + getPosition() + " " + frame.getPosition());
				return;
			}
		}
		setPosition(0);
	}

	public int makeSnapshot() {
		if (isPlaying()) {
			togglePlay();
		}
		int position = getPosition();
		getLogger().debug("Position found :" + position);
		setPosition(position);
		position = getPosition();
		getLogger().debug("Final position :" + position);
		getRecording().addKeyframe(position);
		return position;
	}

	public boolean mute() {
		if (getFiltergraph().getVolume() > 0) {
			volume = getFiltergraph().getVolume();
			getFiltergraph().setVolume(0);
			return true;
		} else {
			getFiltergraph().setVolume(volume);
			return false;
		}
	}

	public void increaseVolume() {
		getFiltergraph().setVolume(1.25f * getFiltergraph().getVolume());
	}

	public void decreaseVolume() {
		getFiltergraph().setVolume( getFiltergraph().getVolume() / 1.25f);
	}

	public float getPlayRate() {
		return PLAY_RATES[playRateIndex];
	}

	private void setPlayRateIndex(int rate) {
		this.playRateIndex = rate;
		AppSettings.getInstance().setRateIndex(this.playRateIndex);
		getFiltergraph().setRate(getPlayRate());
	}

	private void setPlaying(boolean playing) {
		firePropertyChange(PLAYING, this.playing, this.playing = playing);
	}

	public boolean isPlaying() {
		return playing;
	}

	public int getPosition(){
		return getFiltergraph().getTime();
	}

	public void setPosition(int position) {
		getFiltergraph().setTimeValue(position);
		firePropertyChange(POSITION, this.position, this.position = position);
	}

	@Override
	public String getName() {
		return getResourceMap().getString("windowTitle.text", playerId);
	}


}
