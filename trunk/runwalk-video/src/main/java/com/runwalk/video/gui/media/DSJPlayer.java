package com.runwalk.video.gui.media;

import java.io.File;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;

import com.runwalk.video.RunwalkVideoApp;

import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSMovie;


public class DSJPlayer extends DSJComponent<DSMovie> implements IVideoPlayer {

	private boolean customFramerateEnabled = false;

	private float framerate;

	private float rate;

	private boolean playing;

	public boolean loadFile(File videoFile) {
		boolean rebuilt = false;
		String path = videoFile.getAbsolutePath();
		try {
			if (rebuilt = getFiltergraph() == null) {
				initFiltergraph(path);
			} else {
				getFiltergraph().loadFile(path , 0);
			}
		} catch(DSJException e) {
			rebuilt = true;
			/*JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
					"Er heeft zich een fout voorgedaan bij het openen van een filmpje.\n" +
					"Probeer het gerust nog eens opnieuw.",
					"Fout bij openen filmpje",
					JOptionPane.ERROR_MESSAGE);*/
			Logger.getLogger(DSJPlayer.class).error("Movie initialization failed.", e);
			dispose();
			initFiltergraph(path);
		} 
		return rebuilt;
	}

	private void initFiltergraph(String path) {
		Logger.getLogger(DSJPlayer.class).debug("Movie path opened: " + path);
		int flags = DSFiltergraph.DD7 | DSMovie.INIT_PAUSED;
		if (customFramerateEnabled) {
			flags = flags | DSMovie.INIT_EDITABLE;
		}
		setFiltergraph(new DSMovie(path, flags, null));
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

	public void pause() {
		getFiltergraph().pause();
		playing = false;
	}

	public void stop() {
		getFiltergraph().stop();
		getFiltergraph().setTimeValue(0);
		setPlaying(false);
	}

	public int getPosition(){
		return getFiltergraph().getTime();
	}

	public void setPosition(int position) {
		getFiltergraph().setTimeValue(position);
	}

	public void play() {
		getFiltergraph().setRate(this.rate);
		setPlaying(true);
	}
	
	private void setPlaying(boolean playing) {
		this.playing = playing;
	}
	
	private boolean isPlaying() {
		return playing;
	}
	
	public void setPlayRate(float rate) {
		this.rate = rate;
		//TODO check whether this movie is playing or not.. dit geeft problemen als je de slower/faster acties gebruikt!!
		if (isPlaying()) {
			getFiltergraph().setRate(rate);
		}
	}

	public float getPlayRate() {
		return this.rate;
	}

	public float getVolume() {
		return getFiltergraph().getVolume();
	}
	
	public void setVolume(float volume) {
		getFiltergraph().setVolume(volume);
	}

	public String getTitle() {
		return "DSJ player";
	}


}
