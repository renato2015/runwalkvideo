package com.runwalk.video.media.dsj;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;

import org.jdesktop.application.Action;

import com.runwalk.video.media.IVideoPlayer;
import com.runwalk.video.media.settings.VideoPlayerSettings;

import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSJUtils;
import de.humatic.dsj.DSMovie;

public class DSJPlayer extends DSJComponent<DSMovie> implements IVideoPlayer {

	private boolean customFramerateEnabled = false;

	private int fourCc = -1;
	
	private VideoPlayerSettings videoPlayerSettings;
	
	public DSJPlayer(VideoPlayerSettings videoPlayerSettings) {
		this.videoPlayerSettings = videoPlayerSettings;
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
				// do not reload video if fourCc is not the same
				int[] fileStats = DSJUtils.getBasicFileStats(path);
				int fourCc = fileStats[5];
				if (fourCc != -1 && this.fourCc == fourCc) {
					rebuilt = getFiltergraph().loadFile(path , 0) < 0;
				} else {
					getLogger().debug("FourCc was " + this.fourCc + " and will be set to " + fourCc);
					rebuilt = rebuildFiltergraph(path, flags, listener);
				}
				this.fourCc = fourCc;
			}
		} catch(DSJException e) {
			getLogger().error("Recording initialization failed.", e);
			rebuilt = rebuildFiltergraph(path, flags, listener);
		}
		return rebuilt;
	}
	
	private boolean rebuildFiltergraph(String path, int flags, PropertyChangeListener listener) {
		// TODO clean this up.. should be done better
		dispose();
		initFiltergraph(path, flags, listener);
		return true;
	}
	
	private void initFiltergraph(String path, int flags, PropertyChangeListener listener) {
		getLogger().debug("Recording opened from: " + path);
		if (isCustomFramerateEnabled()) {
			flags = flags | DSMovie.INIT_EDITABLE;
		}
		setFiltergraph(new DSMovie(path, flags, listener));
		// set playrate to 0 to make sure the filtergraph wont start running
		pause();
		if (isCustomFramerateEnabled()) {
			getFiltergraph().setMasterFrameRate(videoPlayerSettings.getFrameRate());
		}
		getFiltergraph().setRecueOnStop(true);
	}

	@Action
	public void setCustomFramerate() {
		Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		try {
			if (getFiltergraph() != null && getFiltergraph().getActive()) {
				setCustomFramerateEnabled(true);
				String prefferredRate = JOptionPane.showInputDialog(activeWindow, 
						getResourceMap().getString("setCustomFramerate.dialog.title"), 
						getResourceMap().getString("setCustomFramerate.dialog.text"), 
						JOptionPane.PLAIN_MESSAGE);
				float frameRate = Float.parseFloat(prefferredRate);
				videoPlayerSettings.setFrameRate(frameRate);
				getFiltergraph().setMasterFrameRate(frameRate);
			} else {
				JOptionPane.showMessageDialog(activeWindow, 
						getResourceMap().getString("setCustomFramerate.noActiveGraphDialog.text"));
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(activeWindow, 
					getResourceMap().getString("setCustomFramerate.invalidFpsDialog.text", e.getMessage()));
		}
	}

	public int getDuration() {
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

	public int getPosition() {
		return getFiltergraph().getTime();
	}

	public void setPosition(int position) {
		getFiltergraph().setTimeValue(position);
	}

	public void play() {
		setPlayRate(videoPlayerSettings.getPlayRate());
	}
	
	public void setPlayRate(float rate) {
		videoPlayerSettings.setPlayRate(rate);
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
	
	public boolean isCustomFramerateEnabled() {
		return customFramerateEnabled;
	}

	public void setCustomFramerateEnabled(boolean customFramerateEnabled) {
		this.customFramerateEnabled = customFramerateEnabled;
	}

	public String getTitle() {
		return videoPlayerSettings.getName();
	}

}
