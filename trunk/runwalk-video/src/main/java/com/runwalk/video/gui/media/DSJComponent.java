package com.runwalk.video.gui.media;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;

import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFilter;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSMovie;

/**
 * This class bundls all common DSJ functionality for the {@link IVideoCapturer} and {@link IVideoPlayer} implementations.
 * 
 * @author Jeroen Peelaerts
 *
 * @param <T> The specific DSFiltergraph subclass used by this component
 */
public abstract class DSJComponent<T extends DSFiltergraph> implements IVideoComponent {
	
	/**
	 * D3D9 renderer uses newer DirectX API and less CPU than the former when it can work on a capable GPU.
	 * All filtergraphs are initialized in paused state.
	 */
	protected static final int FLAGS = DSFiltergraph.D3D9 | DSMovie.INIT_PAUSED;

	static {
		// initialize and unlock dsj dll at class loading time
		DSEnvironment.setDebugLevel(4);
		DSEnvironment.unlockDLL("jeroen.peelaerts@vaph.be", 610280, 1777185, 0);
	}
	
	private T filtergraph;

	private boolean rejectPauseFilter = false;

	/**
	 * Constructor for fullscreen mode.
	 * 
	 * @param device the {@link GraphicsDevice} on which the {@link Frame} will be displayed
	 */
	public DSJComponent(GraphicsDevice device) { }

	public DSJComponent() { }

	public T getFiltergraph() {
		return filtergraph;
	}

	public void setFiltergraph(T graph) {
		this.filtergraph = graph;
	}

	public boolean getRejectPauseFilter() {
		return rejectPauseFilter;
	}

	//TODO dit zou een actie kunnen worden!! de waarde van die checkbox kan je uit een UI element halen.
	public void setRejectPauseFilter(boolean rejectPauseFilter) {
		this.rejectPauseFilter = rejectPauseFilter;
		getLogger().debug("Pause filter rejection for filtergraph " + getTitle() + " now set to " + rejectPauseFilter);
	}

	protected Logger getLogger() {
		return Logger.getLogger(DSJComponent.class);
	}

	@Action
	public void viewFilterProperties() {
		DSFilter[] filters = getFiltergraph().listFilters();
		String[] filterInfo = new String[filters.length];
		for(int i  = 0; i < filters.length; i++) {
			filterInfo[i] = filters[i].getName();
		}
		String selectedString =  (String) JOptionPane.showInputDialog(
				null,
				"Kies een filter:",
				"Bekijk filter..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				filterInfo,
				filterInfo[0]);
		if (selectedString != null) {
			int selectedIndex = Arrays.asList(filterInfo).indexOf(selectedString);
			DSFilter selectedFilter = filters[selectedIndex];
			selectedFilter.showPropertiesDialog();
		}
	}

	//@Action
	//TODO hier een actie van maken..
	public void insertFilter(String name) {
		DSFilterInfo filterinfo = DSFilterInfo.filterInfoForName(name);
		if (getFiltergraph() != null) {
			DSFilter[] installedFilters = getFiltergraph().listFilters();
			DSFilter filter = getFiltergraph().addFilterToGraph(filterinfo);
			filter.connectDownstream(filter.getPin(0,0), installedFilters[1].getPin(1, 0), true);
			filter.dumpConnections();
			getLogger().debug("Inserting filter before " + installedFilters[1].getName() + " after " + installedFilters[3].getName());
			getFiltergraph().insertFilter(installedFilters[1], installedFilters[3], filterinfo);
			filter.showPropertiesDialog();
		}
	}

	public void dispose() {
		if (getFiltergraph() != null) {
			Frame fullscreenFrame = getFiltergraph().getFullScreenWindow();
			if (fullscreenFrame != null) {
				fullscreenFrame.dispose();
			}
			getFiltergraph().dispose();
		}
	}

	public boolean isActive() {
		return getFiltergraph() != null && getFiltergraph().getActive();
	}

	public Component getComponent() {
		return getFiltergraph().asComponent();
	}

	public Frame getFullscreenFrame() {
		return getFiltergraph().getFullScreenWindow();
	}

	public void setFullScreen(GraphicsDevice device, boolean fullscreen) {
		if (fullscreen) {
			getFiltergraph().goFullScreen(device, 1);
		} else {
			getFiltergraph().leaveFullScreen();
		}
	}

}
