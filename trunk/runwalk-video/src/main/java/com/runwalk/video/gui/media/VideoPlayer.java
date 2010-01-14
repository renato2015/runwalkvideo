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
import com.runwalk.video.util.ApplicationSettings;

import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSMovie;


public class VideoPlayer extends VideoComponent<DSMovie> {

	public static final String POSITION = "position";

	private static final String ENABLE_CUSTOM_FRAMERATE = "enableCustomFramerate";

	public static final String PLAYING = "playing";

	private final static float[] PLAY_RATES = ApplicationSettings.PLAY_RATES;

	private boolean playing = false;

	private int rateIndex = ApplicationSettings.getInstance().getSettings().getRateIndex();

	private boolean enableFramerate = false;

	private float framerate;

	private int position, timePlaying;

	private float volume;
	
	public VideoPlayer(PropertyChangeListener listener, Recording recording) {
		super(listener);
		setTimer(new Timer(50, null));
		getTimer().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isPlaying()) {
					if (getPosition() == 0) {
						stop();
					}
					firePropertyChange(POSITION, timePlaying, timePlaying = getPosition());
				}
			}
		});
		playFile(recording);
	}

	public void playFile(Recording recording) {
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
			JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
					"Er heeft zich een fout voorgedaan bij het openen van een filmpje.\n" +
					"Probeer het gerust nog eens opnieuw.",
					"Fout bij openen filmpje",
					JOptionPane.ERROR_MESSAGE);
			getLogger().error("Movie initialization failed.", e);
			disposeFiltergraph();
			initFiltergraph(path);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
					"Het bestand dat u probeerde te openen kon niet worden gevonden",
					"Fout bij openen filmpje",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void initFiltergraph(String path) {
		getLogger().debug("Movie path opened : " + path);
		int flags = DSFiltergraph.D3D9 | DSMovie.INIT_PAUSED;
		if (enableFramerate) {
			flags = flags | DSMovie.INIT_EDITABLE;
		}
		setFiltergraph(new DSMovie(path, flags, getPropertyChangeListeners()[0]));
		if (enableFramerate) {
			getFiltergraph().setMasterFrameRate(framerate);
		}
		getFiltergraph().lockAspectRatio(true);
		getFiltergraph().setRecueOnStop(true);
	}

	@Action(enabledProperty=ENABLE_CUSTOM_FRAMERATE)
	public void setFrameRate() {
		try {
			if (getFiltergraph() != null && getFiltergraph().getActive()) {
				enableFramerate = true;
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
			getFiltergraph().setRate(getRate());
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
		if (isPlaying() && rateIndex < PLAY_RATES.length - 1) {
			setRate(++rateIndex);
		} else if (!isPlaying()) {
			togglePlay();
		}
		getFiltergraph().setRate(getRate());
	}

	public void backward() {
		if (rateIndex > 0) {
			setRate(--rateIndex);
			if (isPlaying()) {
				getFiltergraph().setRate(getRate());
				ApplicationSettings.getInstance().getSettings().setRateIndex(rateIndex);
			}
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

	public float getRate() {
		return PLAY_RATES[rateIndex];
	}

	public void setRate(int rate) {
		this.rateIndex = rate;
		getFiltergraph().setRate(getRate());
	}

	public void setPlaying(boolean playing) {
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
		return getRecording().getVideoFileName();
	}


}
