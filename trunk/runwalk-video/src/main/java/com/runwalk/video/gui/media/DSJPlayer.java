package com.runwalk.video.gui.media;

import java.awt.Color;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;

import org.jdesktop.application.Action;

import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSMovie;

public class DSJPlayer extends DSJComponent<DSMovie> implements IVideoPlayer {

	private boolean customFramerateEnabled = false;

	private float framerate;

	private float rate;

	public DSJPlayer(float rate) {
		this.rate = rate;
	}
	
	public DSJPlayer(String path, int flags, PropertyChangeListener listener) {
		loadVideo(path, flags, listener);
	}
	
	public boolean loadVideo(String path) {
		return loadVideo(path, FLAGS, null);
	}
	
	public boolean loadVideo(String path, int flags, PropertyChangeListener listener) {
		boolean rebuilt = false;
		try {
			if (rebuilt = getFiltergraph() == null) {
				initFiltergraph(path, flags, listener);
			} else {
				rebuilt = getFiltergraph().loadFile(path , 0) < 0;
			}
		} catch(DSJException e) {
			rebuilt = true;
			getLogger().error("Recording initialization failed.", e);
			dispose();
			initFiltergraph(path, flags, listener);
		} 
		return rebuilt;
	}
	
	private void initFiltergraph(String path, int flags, PropertyChangeListener listener) {
		getLogger().debug("Recording opened from: " + path);
		if (customFramerateEnabled) {
			flags = flags | DSMovie.INIT_EDITABLE;
		}
		setFiltergraph(new DSMovie(path, flags, listener));
		// set playrate to 0 to make sure the filtergraph wont start running
		pause();
		if (customFramerateEnabled) {
			getFiltergraph().setMasterFrameRate(framerate);
		}
		getFiltergraph().setRecueOnStop(true);
	}

	@Action
	public void setCustomFramerate() {
		try {
			if (getFiltergraph() != null && getFiltergraph().getActive()) {
				customFramerateEnabled = true;
				String prefferredRate = JOptionPane.showInputDialog(null, 
						"Geef een framerate in..", "Set framerate op capture device", JOptionPane.PLAIN_MESSAGE);
				framerate = Float.parseFloat(prefferredRate);
				getFiltergraph().setMasterFrameRate(framerate);
			} else {
				JOptionPane.showMessageDialog(null, 
				"Geen actieve filtergraph gevonden..");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, 
					"Framerate was ongeldig: " + e.getMessage());
		}
	}

	public int getDuration(){
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

	public int getPosition(){
		return getFiltergraph().getTime();
	}

	public void setPosition(int position) {
		getFiltergraph().setTimeValue(position);
	}

	public void play() {
		setPlayRate(rate);
	}
	
	public void setPlayRate(float rate) {
		this.rate = rate;
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

	public String getTitle() {
		return "DSJ player";
	}

}
