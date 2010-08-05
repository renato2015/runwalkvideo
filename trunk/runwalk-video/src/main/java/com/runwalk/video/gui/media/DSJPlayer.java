package com.runwalk.video.gui.media;

import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;

import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSMovie;


public class DSJPlayer extends DSJComponent<DSMovie> implements IVideoPlayer {

	private static final int FLAGS = DSFiltergraph.DD7 | DSMovie.INIT_PAUSED;

	private boolean customFramerateEnabled = false;

	private float framerate;

	private float rate;

	public DSJPlayer(float rate) {
		this.rate = rate;
	}

	public DSJPlayer(File videoFile, int flags, PropertyChangeListener listener) {
		loadFile(videoFile, flags, listener);
	}
	
	/** {@inheritDoc} */
	public boolean loadFile(File videoFile) {
		return loadFile(videoFile, FLAGS, null);
	}
	
	public boolean loadFile(File videoFile, int flags, PropertyChangeListener listener) {
		boolean rebuilt = false;
		String path = videoFile.getAbsolutePath();
		try {
			if (rebuilt = getFiltergraph() == null) {
				initFiltergraph(path, flags, listener);
			} else {
				rebuilt = getFiltergraph().loadFile(path , 0) < 0;
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
			initFiltergraph(path, flags, listener);
		} 
		return rebuilt;
	}
	
	private void initFiltergraph(String path, int flags, PropertyChangeListener listener) {
		Logger.getLogger(DSJPlayer.class).debug("Movie path opened: " + path);
		if (customFramerateEnabled) {
			flags = flags | DSMovie.INIT_EDITABLE;
		}
		setFiltergraph(new DSMovie(path, flags, listener));
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

	public void pause() {
		getFiltergraph().pause();
	}
	
	public void stop() {
		// it seems to be more appropriate to pause the filtergraph instead of stopping it
		getFiltergraph().pause();
		getFiltergraph().setTimeValue(0);
	}

	public int getPosition(){
		return getFiltergraph().getTime();
	}

	public void setPosition(int position) {
		getFiltergraph().setTimeValue(position);
	}

	public void play() {
		getFiltergraph().setRate(rate);
	}
	
	public void setPlayRate(float rate) {
		this.rate = rate;
		getFiltergraph().setRate(rate);
	}

	public float getPlayRate() {
		return rate;
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
