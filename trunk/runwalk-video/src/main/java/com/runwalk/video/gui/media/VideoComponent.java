package com.runwalk.video.gui.media;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JInternalFrame;
import javax.swing.Timer;
import javax.swing.event.InternalFrameListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task.BlockingScope;

import com.google.common.collect.Lists;
import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.AppInternalFrame;
import com.runwalk.video.gui.AppWindowWrapper;
import com.runwalk.video.util.AppUtil;

/**
 * This abstraction allows you to make easy reuse of the vendor independent logic 
 * used by the components that implement {@link IVideoPlayer} and {@link IVideoCapturer}.
 * 
 * @author Jeroen Peelaerts
 *
 */
public abstract class VideoComponent extends AbstractBean implements AppWindowWrapper {

	public static final String FULLSCREEN = "fullscreen";
	public static final String IDLE = "idle";
	public static final String FULL_SCREEN_ENABLED = "fullScreenEnabled";
	public static final String STATE = "state";
	public static final String MONITOR_ID = "monitorId";

	private List<AppWindowWrapperListener> appWindowWrapperListeners = Lists.newArrayList();
	private Recording recording;
	private AppInternalFrame internalFrame;
	private Timer timer;
	private boolean fullscreen = false;
	private ActionMap actionMap;
	private State state;
	private Integer monitorId;
	private boolean fullScreenEnabled;
	
	/**
	 * This variable is used to determine the default monitor on which this component will be shown.
	 */
	private final int componentId;
	
	/**
	 * This method returns a monitor number for a given amount of monitors and a given {@link VideoComponent} instance number.
	 * The resulting number will be used for showing a {@link VideoCapturer} or {@link VideoPlayer} instance, 
	 * which both are uniquely numbered.
	 * 
	 * <ul>
	 * <li>If the total number of available monitors is smaller than 2, then the last monitor index will be used at all times.</li>
	 * <li>If the total number of available monitors is greater than 2, then the assigned monitor index will alternate between 1 and the last
	 * monitor index according to the value of the componentId parameter.</li>
	 * </ul>
	 * 
	 * @param graphicsDevicesCount The amount of available screens
	 * @param componentId The instance number
	 * @return The screen id
	 */
	public static int getDefaultScreenId(int graphicsDevicesCount, int componentId) {
		int result = graphicsDevicesCount - 1;
		if (graphicsDevicesCount > 2) {
			int availableScreenCount = graphicsDevicesCount - 1;
			result = 0;
			// assign a different (alternating) monitor for each instance if there are more than 2 monitors in total
			for (int i = 1; i <= componentId; i++) {
				if (result >= availableScreenCount) {
					result = 1;
				} else {
					result ++;
				}
			}
		}
		return result;
	}

	public VideoComponent(PropertyChangeListener listener, int componentId) {
		this.componentId = componentId;
		addPropertyChangeListener(listener);
		setState(State.IDLE);
	}

	//TODO review code!
	protected void reAttachAppWindowWrapperListeners() {
		if (isFullscreen() && getFullscreenFrame() != null) {
			for(AppWindowWrapperListener appWindowWrapperListener : appWindowWrapperListeners) {
				List<WindowListener> listeners = Arrays.asList(getFullscreenFrame().getWindowListeners());
				if (!listeners.contains(appWindowWrapperListener)) {
					getFullscreenFrame().addWindowListener(appWindowWrapperListener);
				}
			}
		}
		if (!isFullscreen() && getInternalFrame() != null) {
			for(AppWindowWrapperListener appWindowWrapperListener : appWindowWrapperListeners) {
				List<InternalFrameListener> listeners = Arrays.asList(getInternalFrame().getInternalFrameListeners());
				if (!listeners.contains(appWindowWrapperListener)) {
					getInternalFrame().addInternalFrameListener(appWindowWrapperListener);
				}
			}
		}
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

	public List<AppWindowWrapperListener> getAppWindowWrapperListeners() {
		return appWindowWrapperListeners;
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

	/**
	 * Returns an unique id for this concrete {@link VideoComponent} type.
	 * Implementations can be numbered independently from each other.
	 * Most of the time a static counter will be used at creation time, that 
	 * is incremented in the subclass 
	 * 
	 * @return The number
	 */
	public int getComponentId() {
		return componentId;
	}

	/**
	 * Set the title of the component that is currently shown, which will become
	 * visible in the top of the window frame.
	 * 
	 * @param title The title
	 */
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

	protected void setMonitorId(int monitorId) {
		this.monitorId = monitorId;
	}

	public boolean isFullscreen() {
		return fullscreen;
	}
	
	protected void showComponent() {
		showComponent(null);
	}
	
	protected void showComponent(Integer monitorId) {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		int defaultScreenId = getDefaultScreenId(graphicsDevices.length, getComponentId());
		// check preconditions
		if (monitorId != null && monitorId < graphicsDevices.length) {
			// use monitor screen set by user
			getLogger().log(Level.INFO, "Monitor number " + monitorId + " selected for " + getTitle() + ".");
		} else {
			// use default monitor screen, because it wasn't set or found to be invalid
			monitorId = defaultScreenId;
			getLogger().log(Level.WARN, "Default monitor number " + monitorId + " selected for " + getTitle() + ".");
		}
		GraphicsDevice graphicsDevice = graphicsDevices[monitorId];
		// go fullscreen if the selected monitor is not the primary one (index 0)
		boolean fullscreen = monitorId > 0;
		if (monitorId > 0) {
			getApplication().hideComponent(internalFrame);
			getVideoImpl().setFullScreen(graphicsDevice, true);
		} else {
			getVideoImpl().setFullScreen(graphicsDevice, false);
			if (internalFrame == null) {
				internalFrame = new AppInternalFrame(getTitle(), false);
				internalFrame.add(getVideoImpl().getComponent());
			}
		}
		firePropertyChange(FULLSCREEN, this.fullscreen, this.fullscreen = fullscreen);
		getApplication().createOrShowComponent(this);
		reAttachAppWindowWrapperListeners();
		setComponentTitle(getTitle());
	}

	@org.jdesktop.application.Action(enabledProperty=FULL_SCREEN_ENABLED, block=BlockingScope.APPLICATION)
	public void toggleFullscreen() {
		// go fullscreen if component is displaying on the primary device, otherwise apply windowed mode
		monitorId = monitorId != null && monitorId == 0 ? null : 0;
		showComponent(monitorId);
	}

	protected void setState(State state) {
		firePropertyChange(STATE, this.state, this.state = state);
		// full screen mode is enabled for this component if there are at least 2 monitors connected and the component is idle
		boolean isIdle = state == State.IDLE;
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		setFullScreenEnabled(isIdle && graphicsDevices.length > 1);
	}

	public State getState() {
		return state;
	}
	
	public void setFullScreenEnabled(boolean fullScreenEnabled) {
		firePropertyChange(FULL_SCREEN_ENABLED, this.fullScreenEnabled, this.fullScreenEnabled = fullScreenEnabled);
	}
	
	public boolean isFullScreenEnabled() {
		return fullScreenEnabled;
	}

	public boolean isIdle() {
		return getState() == State.IDLE;
	}

	public void dispose() {
		// remove window listeners on the windows to make them eligible for garbage collection
		for (AppWindowWrapperListener listener : getAppWindowWrapperListeners()) {
			removeAppWindowWrapperListener(listener);
		}
		// remove property change listeners to make this instance eligible for garbage collection
		for (PropertyChangeListener listener : getPropertyChangeListeners()) {
			removePropertyChangeListener(listener);
		}
		if (getVideoImpl() != null) {			
			getVideoImpl().dispose();
		}
		setRecording(null);
	}

	public String getTitle() {
		return getVideoImpl().getTitle();
	}

	public abstract IVideoComponent getVideoImpl();
	
//	public abstract void setVideoImpl(IVideoComponent videoImpl);

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

	protected Logger getLogger() {
		return Logger.getLogger(getClass());
	}

	public ResourceMap getResourceMap() {
		return getContext().getResourceMap(getClass(), VideoComponent.class);
	}

	/**
	 * Merge the {@link ActionMap} of the implementation with the one of this instance..
	 */
	public ActionMap getApplicationActionMap() {
		if (actionMap == null) {
			ActionMap actionMap = getContext().getActionMap(VideoComponent.class, this);
			if (getVideoImpl() == null) {
				return actionMap;
			}
			Class<?> videoComponentImplementor = getActionSuperClass(getVideoImpl().getClass());
			ActionMap videoImplActionMap = getContext().getActionMap(videoComponentImplementor, getVideoImpl());
			this.actionMap = AppUtil.mergeActionMaps(actionMap, videoImplActionMap);
		}
		return actionMap;
	}
	
	private Class<?> getActionSuperClass(Class<?> theClass) {
		List<Class<?>> interfaces = Arrays.asList(theClass.getInterfaces());
		if (!interfaces.contains(IVideoComponent.class)) {
			return getActionSuperClass(theClass.getSuperclass());
		}
		return theClass;
	}

	public enum State {
		PLAYING, RECORDING, IDLE
	}

}