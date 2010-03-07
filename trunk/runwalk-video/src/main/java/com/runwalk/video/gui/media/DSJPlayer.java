package com.runwalk.video.gui.media;

import java.awt.GraphicsDevice;

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

	private float volume;

	private float rate;

	public void loadFile(String path) {
		try {
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
			Logger.getLogger(DSJPlayer.class).error("Movie initialization failed.", e);
			dispose();
			initFiltergraph(path);
		} 
	}

	private void initFiltergraph(String path) {
		Logger.getLogger(DSJPlayer.class).debug("Movie path opened: " + path);
		int flags = DSFiltergraph.DD7 | DSMovie.INIT_PAUSED;
		if (customFramerateEnabled) {
			flags = flags | DSMovie.INIT_EDITABLE;
		}
		//FIXME the propertychangelistener is not used anymore here!!
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
	}

	public void stop() {
		getFiltergraph().stop();
		getFiltergraph().setTimeValue(0);
	}

	public int getPosition(){
		return getFiltergraph().getTime();
	}

	public void setPosition(int position) {
		getFiltergraph().setTimeValue(position);
	}

	public void play() {
		getFiltergraph().play();
		getFiltergraph().setRate(this.rate);
	}
	
	public void setPlayRate(float rate) {
		this.rate = rate;
		getFiltergraph().setRate(rate);
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

	public void setMuted(boolean mute) {
		volume = getVolume();
		getFiltergraph().setVolume(mute ? 0 : volume);
	}

	public boolean isMuted() {
		return getFiltergraph().getVolume() == 0;
	}

	public String getName() {
		return "DSJ player";
	}


}
