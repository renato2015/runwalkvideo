package com.runwalk.video.gui.media;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.JInternalFrame;
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

	public static final String FULLSCREEN = "fullscreen";
	private Recording recording;
	private Frame fullScreenFrame;
	private T filtergraph;
	private boolean rejectPauseFilter = false;
	private Timer timer;
	private MyInternalFrame internalFrame;
	private boolean fullscreen = false;

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
	
	//TODO dit zou een actie kunnen worden!! de waarde van die checkbox kan je uit een UI element halen.
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
	
	public JInternalFrame getInternalFrame() {
		return internalFrame.getComponent();
	}
	
	protected void setComponentTitle(String title) {
		if (isFullscreen()) {
			getFullscreenFrame().setTitle(title);
		} else {
			getInternalFrame().setTitle(title);
		}
	}

	//TODO dit zou een actie moeten worden, het GraphicsDevice moet vooraf gekozen worden!
	//TODO eventueel nakijken hoe heet afsluiten of aansluiten van een scherm kan opgevangen worden
	public void toggleFullscreen(GraphicsDevice device) {
		if (isFullscreen()) {
			getFiltergraph().leaveFullScreen();
			if (internalFrame == null) {
				internalFrame = new MyInternalFrame(getName(), false);
				internalFrame.add(getFiltergraph().asComponent());
			} else {
				internalFrame.setVisible(true);
			}
		} else {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			if (gs.length > 1) {
				getFiltergraph().goFullScreen(device == null ? gs[1] : device, 1);
				if (internalFrame != null) {
//					internalFrame.remove(getFiltergraph().asComponent());
					internalFrame.setVisible(false);
				}
				fullScreenFrame = getFiltergraph().getFullScreenWindow();
				
			}
		}
		setFullscreen(!isFullscreen());
		setComponentTitle(getName());
		getApplication().addComponent(this);
	}

	public boolean isFullscreen() {
		return this.fullscreen;
	}
	
	public void setFullscreen(boolean fullscreen) {
		this.firePropertyChange(FULLSCREEN, this.fullscreen, this.fullscreen = fullscreen);
	}

	@Override
	public Container getComponent() {
		Container container = null;
		if (isFullscreen()) {
			container = getFullscreenFrame();
		} else {
			container = getInternalFrame();
		}
		return container;
	}

	public void toFront() {
		if (isFullscreen()) {
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

	//@Action
	//TODO hier een actie van maken..
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
	
	/**
	 * TODO deze method ook in de compresstask gebruiken!
	 */
	public void disposeFiltergraph(boolean clearRecording) {
		getApplication().getMenuBar().removeWindow(this);
		AppUtil.disposeDSGraph(getFiltergraph());
		if (clearRecording) {
			setRecording(null);
		}
	}

	public boolean hasRecording() {
		return getRecording() != null;
	}

	public boolean isActive() {
		return getFiltergraph() != null && isVisible() && getFiltergraph().getActive();
	}

}
