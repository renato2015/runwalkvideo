package com.runwalk.video.media.dsj;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;

import com.runwalk.video.media.IVideoCapturer;
import com.runwalk.video.media.IVideoComponent;
import com.runwalk.video.media.IVideoPlayer;
import com.runwalk.video.ui.Containable;
import com.runwalk.video.ui.FullScreenSupport;
import com.runwalk.video.ui.PropertyChangeSupport;

import de.humatic.dsj.DSFilter;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJUtils;
import de.humatic.dsj.rc.RendererControls;

/**
 * This class bundles all common DSJ functionality for the {@link IVideoCapturer} and {@link IVideoPlayer} implementations.
 * 
 * @author Jeroen Peelaerts
 *
 * @param <T> The specific DSFiltergraph subclass used by this component
 */
public abstract class DSJComponent<T extends DSFiltergraph> implements IVideoComponent, PropertyChangeSupport, ComponentListener, FullScreenSupport, Containable, PropertyChangeListener {
	
	private static final String REJECT_PAUSE_FILTER = "rejectPauseFilter";

	/**
	 * D3D9 renderer uses newer DirectX API and less CPU than the former when it can work on a capable GPU.
	 * On the other hand, overlays can only be drawn using DD7's {@link RendererControls}.
	 * All filtergraphs are initialized in the paused state.
	 */
	protected static final int FLAGS = DSFiltergraph.D3D9 | DSFiltergraph.INIT_PAUSED;

	private T filtergraph;

	private boolean rejectPauseFilter = false;

	private boolean fullScreenEnabled;
	
	private boolean fullScreen;

	private boolean visible;
	
	private int monitorId;
	
	/** {@inheritDoc} */
	public void startRunning() {
		// fire a graph changed so all settings made to the filtergraph will be applied
		getFiltergraph().play();
	}
	
	/** {@inheritDoc} */
	public void stopRunning() {
		// stop the filtergraph so we can configure or rewire as needed
		getFiltergraph().stop();
		getLogger().debug("Filtergraph for " + getTitle() + " stopped");
	}
	
	public T getFiltergraph() {
		return filtergraph;
	}

	public void setFiltergraph(T filtergraph) {
		this.filtergraph = filtergraph;
		filtergraph.dumpGraph(true);
		filtergraph.addPropertyChangeListener(this);
	}

	public boolean getRejectPauseFilter() {
		return rejectPauseFilter;
	}

	public void setRejectPauseFilter(boolean rejectPauseFilter) {
		// a PCE will be fired to make the MenuItem respond to changes to this property
		firePropertyChange(REJECT_PAUSE_FILTER, this.rejectPauseFilter, this.rejectPauseFilter = rejectPauseFilter);
		getLogger().debug("Pause filter rejection for filtergraph " + getTitle() + " now set to " + rejectPauseFilter);
	}
	
	@Action(selectedProperty = REJECT_PAUSE_FILTER)
	public void toggleRejectPauseFilter() {
		// this action will be shown as a JCheckBoxMenuItem in the menu bar
	}

	public Logger getLogger() {
		return Logger.getLogger(getClass());
	}

	@Action
	public void viewFilterProperties() {
		DSFilter[] filters = getFiltergraph().listFilters();
		DSFilter selectedFilter =  (DSFilter) JOptionPane.showInputDialog(
				null,
				"Kies een filter:",
				"Bekijk filter..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				filters,
				filters[0]);
		if (selectedFilter != null) {
			selectedFilter.showPropertiesDialog();
		}
	}

	//@Action
	//TODO hier een actie van maken..
	public void insertFilter(String name) {
		DSFilterInfo filterinfo = DSFilterInfo.filterInfoForName(name);
		if (getFiltergraph() != null) {
			DSFilter[] installedFilters = getFiltergraph().listFilters();
			// stop filtergraph to add filters
			getFiltergraph().stop();
			DSFilter filter = getFiltergraph().addFilterToGraph(filterinfo);
			filter.connectDownstream(filter.getPin(0,0), installedFilters[1].getPin(1, 0), true);
			filter.dumpConnections();
			getLogger().debug("Inserting filter before " + installedFilters[1].getName() + " after " + installedFilters[3].getName());
			getFiltergraph().insertFilter(installedFilters[1], installedFilters[3], filterinfo);
			filter.showPropertiesDialog();
			// wiring has changed, notify graph
			getFiltergraph().graphChanged();
			// start playing again
			getFiltergraph().play();
		}
	}

	public void dispose() {
		if (getFiltergraph() != null) {
			// full screen frame needs to disposed on rebuilding..
			getFiltergraph().removePropertyChangeListener(this);
			Frame fullScreenFrame = getFiltergraph().getFullScreenWindow();
			if (fullScreenFrame != null) {
				fullScreenFrame.removeComponentListener(this);
				// TODO make this call or not?
				// fullScreenFrame.dispose();
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
	
	public void setFullScreenEnabled(boolean fullScreenEnabled) {
		firePropertyChange(TOGGLE_FULL_SCREEN_ENABLED, this.fullScreenEnabled, this.fullScreenEnabled = fullScreenEnabled);
	}

	public boolean isFullScreenEnabled() {
		return fullScreenEnabled;
	}
	
	public void setVisible(boolean visible) {
		firePropertyChange(VISIBLE, this.visible, this.visible = visible);
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	@Override
	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		firePropertyChange(FULL_SCREEN, this.fullScreen, this.fullScreen = fullScreen);
	}

	public void toFront() {
		if (getFullscreenFrame() != null) {
			getFullscreenFrame().toFront();
		}
	}
	
	@Action(selectedProperty = VISIBLE)
	public void toggleVisibility() {
		// just leave this empty, selected property will be called..
	}

	public boolean isToggleFullScreenEnabled() {
		return true;
	}

	@Action(selectedProperty = FULL_SCREEN, enabledProperty = TOGGLE_FULL_SCREEN_ENABLED)
	public void toggleFullScreen() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice foundDevice = null;
		for (GraphicsDevice device : graphicsEnvironment.getScreenDevices()) {
			if (device.getIDstring().endsWith(getMonitorId().toString())) {
				foundDevice = device;
			}
		}
		// disable toggling for now.. re-enable when DSJ fires ENTER_FS or EXIT_FS
		setFullScreenEnabled(false);
		if (!fullScreen) {
			getFiltergraph().goFullScreen(foundDevice, 1);
			// TODO install a PCE to listen for ENTER_FS and EXIT_FS
			getFullscreenFrame().setTitle(getTitle());
			getFullscreenFrame().setName(getTitle());
		} else {
			getFiltergraph().leaveFullScreen();
		}
	}
	
	public Integer getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(Integer monitorId) {
		this.monitorId = monitorId;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		int eventType = DSJUtils.getEventType(evt);
		if (eventType == DSFiltergraph.GRAPH_EVENT ) {
			int eventCode = DSJUtils.getEventValue_int(evt);
			if (eventCode == DSFiltergraph.ENTER_FS) {
				setFullScreen(true);
				getLogger().debug("Fullscreen mode entered for " + getTitle());
				setFullScreenEnabled(true);
			} else if (eventCode == DSFiltergraph.EXIT_FS) {
				setFullScreen(false);
				getLogger().debug("Fullscreen mode exited for " + getTitle());
				setFullScreenEnabled(true);
			}
		}
	}
	
	public void componentShown(ComponentEvent e) {
		setVisible(e.getComponent().isVisible());
	}

	public void componentHidden(ComponentEvent e) {
		setVisible(e.getComponent().isVisible());
	}

	public void componentResized(ComponentEvent e) { }

	public void componentMoved(ComponentEvent e) { }

	public BufferedImage getImage() {
		return getFiltergraph().getImage();
	}
	
	public Dimension getDimension() {
		return getFiltergraph().getDisplaySize();
	}

	public void setOverlayImage(BufferedImage image, Color alphaColor) {
		int width = image == null ? getFiltergraph().getWidth() : image.getWidth();
		int height = image == null ? getFiltergraph().getHeight() : image.getHeight();
		int[] rectangle = new int[] {0, 0, width, height};
		getFiltergraph().getRendererControls().setOverlayImage(image, rectangle, alphaColor, 1f);
	}
	
}
