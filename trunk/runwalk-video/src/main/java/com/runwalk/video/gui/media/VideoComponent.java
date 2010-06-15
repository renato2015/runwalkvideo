package com.runwalk.video.gui.media;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JInternalFrame;
import javax.swing.Timer;
import javax.swing.event.InternalFrameListener;

import org.apache.log4j.Logger;
import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task.BlockingScope;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.AppInternalFrame;
import com.runwalk.video.gui.AppWindowWrapper;
import com.runwalk.video.util.AppUtil;

/**
 * This abstraction allows you to make easy reuse of the vendor independent logic 
 * used by the components that implement {@link IVideoPlayer} and {@link IVideoCapturer}.
 * 
 * @author Jeroen P.
 *
 */
public abstract class VideoComponent extends AbstractBean implements AppWindowWrapper {

	public static final String IDLE = "idle";

	public static final String FULLSCREEN = "fullscreen";

	public static final String STATE = "state";
	
	private Recording recording;
	protected Frame fullScreenFrame;
	protected AppInternalFrame internalFrame;
	private Timer timer;
	boolean fullscreen = false;
	private ActionMap actionMap;
	private State state;
	
	public List<AppWindowWrapperListener> appWindowWrapperListeners = new ArrayList<AppWindowWrapperListener>();

	public VideoComponent(PropertyChangeListener listener) {
		addPropertyChangeListener(listener);
		setState(State.IDLE);
	}
	
	//TODO review code!
	protected void reAttachAppWindowWrapperListeners() {
		if (isFullscreen() && getFullscreenFrame() != null) {
			boolean found = false;
			for (int i = 0; i < appWindowWrapperListeners.size() && !found; i++) {
				AppWindowWrapperListener appWindowWrapperListener = appWindowWrapperListeners.get(i);
				for (WindowListener windowListener : getFullscreenFrame().getWindowListeners()) {
					found = windowListener == appWindowWrapperListener;
				}
				if (!found) {
					getFullscreenFrame().addWindowListener(appWindowWrapperListener);
				}
			}
		}
		if (!isFullscreen() && getInternalFrame() != null) {
			boolean found = false;
			for (int i = 0; i < appWindowWrapperListeners.size() && !found; i++) {
				AppWindowWrapperListener appWindowWrapperListener = appWindowWrapperListeners.get(i);
				for (InternalFrameListener internalFrameListener : getInternalFrame().getInternalFrameListeners()) {
					found = internalFrameListener == appWindowWrapperListener;
				}
				if (!found) {
					getInternalFrame().addInternalFrameListener(appWindowWrapperListener);
				}
			}
		}
	}
	
	public void removeAllWindowWrapperListeners() {
		appWindowWrapperListeners.clear();
	}
	
	public void addAppWindowWrapperListener(AppWindowWrapperListener listener) {
		appWindowWrapperListeners.add(listener);
		if (getFullscreenFrame() != null) {
			getFullscreenFrame().addWindowListener(listener);
		}
		if (getInternalFrame() != null) {
			getInternalFrame().addInternalFrameListener(listener);
		}
	}

	public void removeAppWindowWrapperListener(AppWindowWrapperListener listener) {
		appWindowWrapperListeners.remove(listener);
		if (getFullscreenFrame() != null) {
			getFullscreenFrame().removeWindowListener(listener);
		}
		if (getInternalFrame() != null) {
			getInternalFrame().removeInternalFrameListener(listener);
		}
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

	public Container getHolder() {
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

	//TODO een strategie om te bepalen op welk scherm de fullscreen versie moet getoond worden??
	//TODO eventueel nakijken hoe heet afsluiten of aansluiten van een scherm kan opgevangen worden
	protected void setFullscreen(boolean fullscreen) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		firePropertyChange(FULLSCREEN, this.fullscreen, this.fullscreen = fullscreen);
		if (fullscreen) {
			if (gs.length > 1) {
				getApplication().hideComponent(internalFrame);
				getVideoImpl().setFullScreen(gs[1], fullscreen);
				fullScreenFrame = getVideoImpl().getFullscreenFrame();
			}
		} else {
			getVideoImpl().setFullScreen(gs[0], fullscreen);
			if (internalFrame == null) {
				internalFrame = new AppInternalFrame(getTitle(), false);
				internalFrame.add(getVideoImpl().getComponent());
			}
			getApplication().createOrShowComponent(this);
		}
		reAttachAppWindowWrapperListeners();
		setComponentTitle(getTitle());
	}
	
	@org.jdesktop.application.Action(enabledProperty=IDLE, block=BlockingScope.APPLICATION)
	public void toggleFullscreen() {
		setFullscreen(!isFullscreen());
	}
	
	protected void setState(State newState) {
		State oldState = this.state;
		firePropertyChange(STATE, oldState, this.state = newState);
		firePropertyChange(IDLE, oldState == State.IDLE, newState == State.IDLE);
	}
	
	public State getState() {
		return state;
	}
	
	public boolean isIdle() {
		return getState() == State.IDLE;
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
		return getHolder().isVisible() && getVideoImpl().isActive();
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

	/**
	 * Merge the actinmap of the implementation with the one of this instance..
	 */
	public ActionMap getApplicationActionMap() {
		if (actionMap == null) {
			ActionMap actionMap = getContext().getActionMap(VideoComponent.class, this);
			this.actionMap = AppUtil.mergeActionMaps(actionMap, getVideoImpl().getActionMap());
		}
		return actionMap;
	}
	
	public enum State {
		PLAYING, RECORDING, IDLE
	}

}