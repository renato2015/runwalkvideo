package com.runwalk.video.gui.media;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.jdesktop.application.Action;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.ComponentDecorator;
import com.runwalk.video.gui.MyInternalFrame;
import com.runwalk.video.util.AppUtil;

import de.humatic.dsj.DSFilter;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;

public abstract class VideoComponent<T extends DSFiltergraph> extends ComponentDecorator<Container> {

	private Recording recording;
	private Frame fullScreenFrame;
	private T filtergraph;
	private boolean rejectPauseFilter = false;
	private Timer timer;
	private MyInternalFrame internalFrame;

	/**
	 * Constructor for 'normal' mode
	 */
	public VideoComponent(PropertyChangeListener listener) {
//		super(false);
		addPropertyChangeListener(listener);
	}

	/**
	 * Constructor for fullscreen mode..
	 * @param device the {@link GraphicsDevice} where the Frame will be displayed
	 */
	public VideoComponent(PropertyChangeListener listener, GraphicsDevice device) {
		this(listener);
	}

	public Recording getRecording() {
		return recording;
	}

	public void setRecording(Recording recording) {
		this.recording = recording;
	}

	protected T getFiltergraph() {
		return this.filtergraph;
	}

	protected void setFiltergraph(T graph) {
		this.filtergraph = graph;
	}

	public abstract String getName();

	public boolean getRejectPauseFilter() {
		return rejectPauseFilter;
	}

	public void setRejectPauseFilter(boolean rejectPauseFilter) {
		this.rejectPauseFilter = rejectPauseFilter;
		getLogger().debug("Pause filter rejection for filtergraph " + getName() + " now set to " + rejectPauseFilter);
	}

	protected Timer getTimer() {
		return timer;
	}

	protected void setTimer(Timer timer) {
		this.timer = timer;
	}

	public Frame getFullscreenFrame() {
		return fullScreenFrame;
	}

	public void toggleFullscreen(GraphicsDevice device) {
		if (getFiltergraph().isFullScreen()) {
			getFiltergraph().leaveFullScreen();
			if (internalFrame == null) {
				internalFrame = new MyInternalFrame(getName(), false);
			} else {
				internalFrame.setVisible(true);
			}
			internalFrame.add(getFiltergraph().asComponent());
		} else {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			if (gs.length > 1) {
				if (internalFrame != null) {
//					internalFrame.remove(getFiltergraph().asComponent());
					internalFrame.setVisible(false);
				}
				getFiltergraph().goFullScreen(device == null ? gs[1] : device, 1);
				fullScreenFrame = getFiltergraph().getFullScreenWindow();
				fullScreenFrame.setTitle(getName());
				fullScreenFrame.setName(getName());
			}
		}
		getApplication().addComponent(this);
	}

	@Override
	public Container getComponent() {
		Container container = null;
		if (getFiltergraph().isFullScreen()) {
			container = getFullscreenFrame();
		} else {
			container = internalFrame.getComponent();
		}
		return container;
	}

	public void toFront() {
		if (getFiltergraph().isFullScreen()) {
			getFullscreenFrame().toFront();
		} else {
			internalFrame.getComponent().toFront();
		}
	}

	@Action
	public void viewFilterProperties() {
		DSFilter[] filters = getFiltergraph().listFilters();
		String[] filterInfo = new String[filters.length];
		for(int i  = 0; i < filters.length; i++) {
			filterInfo[i] = filters[i].getName();
		}
		String selectedString =  (String) JOptionPane.showInputDialog(
				RunwalkVideoApp.getApplication().getMainFrame(),
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

	@Action
	public void insertFilter(String name) {
		DSFilterInfo filterinfo = DSFilterInfo.filterInfoForName(name);
		if (getFiltergraph() != null) {
			DSFilter[] installedFilters = getFiltergraph().listFilters();
			DSFilter filter = getFiltergraph().addFilterToGraph(filterinfo);
			filter.showPropertiesDialog();
			filter.connectDownstream(filter.getPin(0,0), installedFilters[1].getPin(1, 0), true);
			filter.dumpConnections();
			getLogger().debug("Inserting filter before " + installedFilters[1].getName() + " after " + installedFilters[3].getName());
			getFiltergraph().insertFilter(installedFilters[1], installedFilters[3], filterinfo);
		}
	}

	public void disposeFiltergraph() {
		AppUtil.disposeDSGraph(getFiltergraph());
		setRecording(null);
	}

	public boolean hasRecording() {
		return getRecording() != null;
	}

	public boolean isActive() {
		return getFiltergraph() != null && isVisible() && getFiltergraph().getActive();
	}

}
