package com.runwalk.video.media.dsj;

import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.beans.PropertyChangeListener;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdesktop.application.Action;

import com.runwalk.video.media.settings.VideoPlayerSettings;

import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSJUtils;
import de.humatic.dsj.DSMovie;

public class DSJPlayer extends AbstractDSJPlayer<DSMovie> {

	private boolean customFramerateEnabled = false;

	private int fourCc = -1;
	
	public DSJPlayer(VideoPlayerSettings videoPlayerSettings) {
		super(videoPlayerSettings);
	}
	
	public DSJPlayer(String path, int flags, PropertyChangeListener listener) {
		loadVideo(path, flags, listener);
	}
	
	public boolean loadVideo(String path) {
		return loadVideo(path, FLAGS, null);
	}
	
	public boolean loadVideo(String path, int flags, PropertyChangeListener listener) {
		boolean rebuilt = getFiltergraph() == null;
		try {
			if (rebuilt) {
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
		if (!isFullScreen()) {
			Container parentFrame = SwingUtilities.getAncestorOfClass(JInternalFrame.class, getComponent());
			parentFrame.remove(getComponent());
			dispose();
			initFiltergraph(path, flags, listener);
			parentFrame.add(getComponent());
		} else {
			dispose();
			initFiltergraph(path, flags, listener);
			enterFullScreen();
		}
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
			getFiltergraph().setMasterFrameRate(getVideoComponentSettings().getFrameRate());
		}
		getFiltergraph().setRecueOnStop(true);
	}

	@Action
	public void setCustomFramerate() {
		Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		try {
			if (getFiltergraph() != null && getFiltergraph().getActive()) {
				setCustomFramerateEnabled(true);
				String preferredRate = JOptionPane.showInputDialog(activeWindow, 
						getResourceMap().getString("setCustomFramerate.dialog.title"), 
						getResourceMap().getString("setCustomFramerate.dialog.text"), 
						JOptionPane.PLAIN_MESSAGE);
				if (preferredRate != null) {
					float frameRate = Float.parseFloat(preferredRate);
					getVideoComponentSettings().setFrameRate(frameRate);
					getFiltergraph().setMasterFrameRate(frameRate);
				}
			} else {
				JOptionPane.showMessageDialog(activeWindow, 
						getResourceMap().getString("setCustomFramerate.noActiveGraphDialog.text"));
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(activeWindow, 
					getResourceMap().getString("setCustomFramerate.invalidFpsDialog.text", e.getMessage()));
		}
	}

	public boolean isCustomFramerateEnabled() {
		return customFramerateEnabled;
	}

	public void setCustomFramerateEnabled(boolean customFramerateEnabled) {
		this.customFramerateEnabled = customFramerateEnabled;
	}

}
