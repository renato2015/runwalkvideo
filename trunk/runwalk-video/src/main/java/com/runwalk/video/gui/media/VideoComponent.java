package com.runwalk.video.gui.media;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JInternalFrame;
import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.AppComponent;
import com.runwalk.video.gui.AppInternalFrame;

/**
 * This abstraction allows you to make easy reuse of the vendor independent logic 
 * used by the components that implement {@link IVideoPlayer} and {@link IVideoCapturer}.
 * 
 * @author Jeroen P.
 *
 */
public abstract class VideoComponent extends AbstractBean implements AppComponent {

	public static final String FULLSCREEN = "fullscreen";

	public static final String CONTROLS_ENABLED = "controlsEnabled";
	
	private Recording recording;
	protected Frame fullScreenFrame;
	protected AppInternalFrame internalFrame;
	private Timer timer;
	boolean fullscreen = false;
	protected boolean controlsEnabled;

	public VideoComponent(PropertyChangeListener listener) {
		addPropertyChangeListener(listener);
	}
	
	public boolean getControlsEnabled() {
		return controlsEnabled;
	}

	public void setControlsEnabled(boolean controlsEnabled) {
		firePropertyChange(CONTROLS_ENABLED, getControlsEnabled(), this.controlsEnabled = controlsEnabled);
	}

	public Recording getRecording() {
		return recording;
	}

	public void setRecording(Recording recording) {
		this.recording = recording;
	}

	protected Timer getTimer() {
		return timer;
	}

	protected void setTimer(Timer timer) {
		this.timer = timer;
	}

	public Frame getFullscreenFrame() {
		return getVideoImpl().getFullscreenFrame();
	}

	public JInternalFrame getInternalFrame() {
		return internalFrame;
	}

	protected void setComponentTitle(String title) {
		if (isFullscreen()) {
			getFullscreenFrame().setTitle(title);
			getFullscreenFrame().setName(title);
		} else {
			getInternalFrame().setName(title);
			getInternalFrame().setTitle(title);
		}
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

	protected boolean hasRecording() {
		return getRecording() != null;
	}

	public boolean isFullscreen() {
		return this.fullscreen;
	}

	protected void setFullscreen(boolean fullscreen) {
		this.firePropertyChange(FULLSCREEN, this.fullscreen, this.fullscreen = fullscreen);
	}
	
	//TODO dit zou een actie moeten worden, het GraphicsDevice moet vooraf gekozen worden!
	//TODO eventueel nakijken hoe heet afsluiten of aansluiten van een scherm kan opgevangen worden
	public void toggleFullscreen(GraphicsDevice device) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		if (isFullscreen()) {
			//getFiltergraph().leaveFullScreen();
			getVideoImpl().toggleFullScreen(gs[0], !isFullscreen());
			if (internalFrame == null) {
				internalFrame = new AppInternalFrame(getTitle(), false);
				internalFrame.add(getVideoImpl().getComponent());
			} else {
				internalFrame.setVisible(true);
			}
		} else {
			if (gs.length > 1) {
				getVideoImpl().toggleFullScreen(gs[1], !isFullscreen());
				//getFiltergraph().goFullScreen(device == null ? gs[1] : device, 1);
				if (internalFrame != null) {
//					internalFrame.remove(getFiltergraph().asComponent());
					internalFrame.setVisible(false);
				}
				fullScreenFrame = getVideoImpl().getFullscreenFrame();
			}
		}
		setFullscreen(!isFullscreen());
		setComponentTitle(getTitle());
		getApplication().addComponent(this);
	}
	
	public void dispose() {
		getVideoImpl().dispose();
		setRecording(null);
	}
	
	public String getTitle() {
		return getVideoImpl().getTitle();
	}
	
	public abstract IVideoComponent getVideoImpl();

	public void toFront() {
		if (isFullscreen()) {
			getFullscreenFrame().toFront();
		} else {
			getInternalFrame().toFront();
		}
	}
	
	public boolean isActive() {
		return getComponent().isVisible() && getVideoImpl().isActive();
	}
	
	public Action getAction(String name) {
		return getApplicationActionMap().get(name);
	}

	public RunwalkVideoApp getApplication() {
		return RunwalkVideoApp.getApplication();
	}

	public ApplicationContext getContext() {
		return getApplication().getContext();
	}

	public Logger getLogger() {
		return Logger.getLogger(getClass());
	}

	public ResourceMap getResourceMap() {
		return getContext().getResourceMap(getClass(), VideoComponent.class);
	}

	public ApplicationActionMap getApplicationActionMap() {
		ApplicationActionMap actionMap = getContext().getActionMap(VideoComponent.class, this);
		//TODO add the actions in the map of the implementation to the ones of this class..
//		ApplicationActionMap parentMap = getContext().getActionMap(DSJComponent.class, getVideoImpl());
		return actionMap;
	}

}