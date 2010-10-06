package com.runwalk.video.gui.media;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.xml.ws.Action;

import org.apache.log4j.Logger;

import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFilter;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.rc.RendererControls;

/**
 * This class bundles all common DSJ functionality for the {@link IVideoCapturer} and {@link IVideoPlayer} implementations.
 * 
 * @author Jeroen Peelaerts
 *
 * @param <T> The specific DSFiltergraph subclass used by this component
 */
public abstract class DSJComponent<T extends DSFiltergraph> implements IVideoComponent {
	
	private static final String DSJ_UNLOCK_NAME = "dsj.unlockName";
	private static final String DSJ_CODE3 = "dsj.code3";
	private static final String DSJ_CODE2 = "dsj.code2";
	private static final String DSJ_CODE1 = "dsj.code1";

	/**
	 * D3D9 renderer uses newer DirectX API and less CPU than the former when it can work on a capable GPU.
	 * On the other hand, overlays can only be drawn using DD7's {@link RendererControls}.
	 * All filtergraphs are initialized in the paused state.
	 */
	protected static final int FLAGS = DSFiltergraph.D3D9 | DSFiltergraph.INIT_PAUSED;

	static {
		// initialize and unlock dsj dll at class loading time
		DSEnvironment.setDebugLevel(4);
		// get dsj unlock code from resource bundle, processed by maven at compile time
		String packageName = DSJComponent.class.getPackage().getName();
		String className = DSJComponent.class.getSimpleName();
		// get class resource bundle using the bsaf naming convention
		ResourceBundle bundle = ResourceBundle.getBundle(packageName + ".resources." + className);
		String unlockName = bundle.getString(DSJ_UNLOCK_NAME);
		Long code1 = Long.parseLong(bundle.getString(DSJ_CODE1));
		Long code2 = Long.parseLong(bundle.getString(DSJ_CODE2));
		Long code3= Long.parseLong(bundle.getString(DSJ_CODE3));
		DSEnvironment.unlockDLL(unlockName, code1, code2, code3);
	}
	
	private T filtergraph;

	private boolean rejectPauseFilter = false;
	
	public T getFiltergraph() {
		return filtergraph;
	}

	public void setFiltergraph(T filtergraph) {
		this.filtergraph = filtergraph;
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
		return Logger.getLogger(getClass());
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
			// TODO review this code.. is this DSJComponent's responsability?
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

	public BufferedImage getImage() {
		return getFiltergraph().getImage();
	}

	public void setOverlayImage(BufferedImage image, Color alphaColor) {
		int width = image == null ? getFiltergraph().getWidth() : image.getWidth();
		int height = image == null ? getFiltergraph().getHeight() : image.getHeight();
		int[] rectangle = new int[] {0, 0, width, height};
		getFiltergraph().getRendererControls().setOverlayImage(image, rectangle, alphaColor, 1f);
	}
	
}
