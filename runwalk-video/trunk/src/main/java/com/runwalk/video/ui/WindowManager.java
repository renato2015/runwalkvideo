package com.runwalk.video.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ActionMap;
import javax.swing.JDesktopPane;
import javax.swing.JRootPane;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationActionMap;

import com.runwalk.video.core.Containable;
import com.runwalk.video.core.FullScreenSupport;
import com.runwalk.video.core.SelfContained;
import com.runwalk.video.core.WindowConstants;
import com.runwalk.video.media.IVideoComponent;
import com.runwalk.video.media.VideoComponent;
import com.runwalk.video.media.VideoComponent.State;
import com.tomtessier.scrollabledesktop.JScrollableDesktopPane;

public class WindowManager implements PropertyChangeListener, WindowConstants {

	private VideoMenuBar menuBar;
	
	private JScrollableDesktopPane pane;

	/**
	 * Returns the default (main) monitor id.
	 * 
	 * @return The default monitor id
	 */
	public static Integer getDefaultMonitorId() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String monitorIdString = graphicsEnvironment.getDefaultScreenDevice().getIDstring();
		monitorIdString = monitorIdString.substring(monitorIdString.length() - 1);
		return Integer.valueOf(monitorIdString);
	}

	public static int getMonitorCount() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
	}

	public static GraphicsDevice getDefaultGraphicsDevice() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		return graphicsEnvironment.getDefaultScreenDevice();
	}

	public WindowManager(VideoMenuBar menuBar, JScrollableDesktopPane pane) {
		this.menuBar = menuBar;
		this.pane = pane;
	}

	public void addWindow(VideoComponent videoComponent) {
		Integer monitorId = videoComponent.getMonitorId();
		if (monitorId == null) {
			monitorId = getDefaultMonitorId();
		}
		addWindow(videoComponent, monitorId);
	}

	public void addWindow(VideoComponent videoComponent, Integer monitorId) {
		videoComponent.addPropertyChangeListener(this);
		IVideoComponent videoImpl = videoComponent.getVideoImpl();
		boolean isContainable = videoImpl instanceof Containable;
		boolean isSelfContained = videoImpl instanceof SelfContained;
		boolean isFullScreenSupported = videoImpl instanceof FullScreenSupport;
		String title = videoComponent.getTitle();
		if (isSelfContained) {
			SelfContained selfContainedImpl = (SelfContained) videoImpl;
			if (isFullScreenSupported && videoComponent.isFocusable()) {
				FullScreenSupport fsVideoImpl = (FullScreenSupport) videoImpl;
				if (!fsVideoImpl.isFullScreen()) {
					// go fullscreen if monitorId not on the default screen
					if (monitorId != null && monitorId != getDefaultMonitorId()) {
						fsVideoImpl.enterFullScreen();
						addWindow(selfContainedImpl, videoComponent.getApplicationActionMap(), title);
					} else if (isContainable) {
						Container container = addWindow((Containable) videoImpl, videoComponent.getApplicationActionMap(), title);
						container.addHierarchyListener(videoComponent.getVideoImpl());
					}
				} else if (isContainable && !fsVideoImpl.isFullScreen()) {
					addWindow((Containable) videoImpl, videoComponent.getApplicationActionMap(), title);
				}
			} else {
				// TODO NPE at VideoComponent.java:188 when trying to use overloaded version with three args
				addWindow(selfContainedImpl, title);
			}
		} else if (isContainable) {
			addWindow((Containable) videoImpl, videoComponent.getApplicationActionMap(), title);
		}
	}

	public void addWindow(SelfContained selfContained, String title) {
		addWindow(selfContained, selfContained.getApplicationActionMap(), title);
	}

	public Container addWindow(Containable containable) {
		return addWindow(containable, containable.getApplicationActionMap(), containable.getTitle());
	}

	private Container addWindow(Containable containable, ApplicationActionMap actionMap, String title) {
		AppInternalFrame selfContainedImpl = createInternalFrame(containable);
		// reeds bestaande actions in action map delegeren naar die van selfContainedImpl
		setActionProxy(actionMap, TOGGLE_VISIBILITY_ACTION, selfContainedImpl.getApplicationActionMap());
		setActionProxy(actionMap, TOGGLE_FULL_SCREEN_ACTION, selfContainedImpl.getApplicationActionMap());
		addWindow(selfContainedImpl, actionMap, title);
		return selfContainedImpl;
	}

	//TODO add windows here to toggle between fullscreen and windowed mode
	private void addWindow(final SelfContained selfContainedImpl, ActionMap actionMap, String title) {
		//TODO enforce constraint here: should only be called once for each window
		if (selfContainedImpl != null) {
			selfContainedImpl.addPropertyChangeListener(this);		
			getMenuBar().addMenu(title, actionMap);
			setVisible(selfContainedImpl, true);
		}
	}

	public void setVisible(SelfContained selfContained, boolean visible) {
		selfContained.setVisible(visible);
	}

	private <T extends SelfContained> T getDecoratingComponent(Class<T> theClass, Containable containable) {
		T result = null;
		Component component = containable != null ? containable.getComponent() : null;
		if (component != null) {
			result = theClass.cast(SwingUtilities.getAncestorOfClass(theClass, component));
		}
		return result;
	}

	private SelfContained getDecoratingComponent(Containable containable) {
		return getDecoratingComponent(SelfContained.class, containable);
	}

	private AppInternalFrame createInternalFrame(Containable containable) {
		AppInternalFrame internalFrame = new AppInternalFrame(containable.getTitle(), containable.isResizable(), containable.getComponent().getMinimumSize());
		internalFrame.add(containable.getComponent());
		internalFrame.pack();
		getPane().add(internalFrame);
		return internalFrame;
	}

	public Container disposeWindow(Containable containable) {
		AppInternalFrame selfContainedImpl = getDecoratingComponent(AppInternalFrame.class, containable);
		if (selfContainedImpl != null) {
			// remove from the desktop pane
			getPane().remove(selfContainedImpl);
			disposeWindow(selfContainedImpl);
		}
		return selfContainedImpl;
	}

	public void disposeWindow(SelfContained selfContainedImpl) {
		if (selfContainedImpl != null) {
			selfContainedImpl.removePropertyChangeListener(this);
			selfContainedImpl.dispose();
		}
	}

	public void disposeWindow(VideoComponent videoComponent) {
		IVideoComponent videoImpl = videoComponent.getVideoImpl();
		// if Containable and not SelfContained then dispose the wrapping component
		// if the implementation implements both SelfContained and Containable, cast to SelfContained
		if (videoImpl instanceof Containable && videoImpl.getMonitorId() == getDefaultMonitorId()) {
			Container container = disposeWindow((Containable) videoImpl);
			container.removeHierarchyListener(videoComponent.getVideoImpl());
		} else if (videoImpl instanceof SelfContained) {
			disposeWindow((SelfContained) videoImpl);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		// disposing windows is handled by listening to the components' eventsget
		if (VideoComponent.STATE.equals(evt.getPropertyName())) {
			VideoComponent.State newState = (State) evt.getNewValue();
			if (VideoComponent.State.DISPOSED.equals(newState)) {
				VideoComponent videoComponent = (VideoComponent) evt.getSource();
				getMenuBar().removeMenu(videoComponent.getTitle());
				videoComponent.removePropertyChangeListener(this);
				disposeWindow(videoComponent);
			}
		} else if (WindowConstants.FULL_SCREEN.equals(evt.getPropertyName())) {
			if (evt.getSource() instanceof Containable) {
				Containable containable = (Containable) evt.getSource();
				Boolean newValue = (Boolean) evt.getNewValue();
				AppInternalFrame selfContainedImpl;
				if (newValue == Boolean.FALSE) {
					// go back to windowed mode
					selfContainedImpl = createInternalFrame(containable);
					// handle SelfContained action proxy's..
					setVisible(selfContainedImpl, true);
				} else {
					selfContainedImpl = getDecoratingComponent(AppInternalFrame.class, containable);
					// remove wrapping container and dispose
					disposeWindow(selfContainedImpl);
				}
				// TODO change action proxy sources as the selfcontained wrapper has been changed
				//setActionProxy(containable.getApplicationActionMap(), TOGGLE_VISIBILITY_ACTION, selfContainedImpl.getApplicationActionMap());
				//setActionProxy(containable.getApplicationActionMap(), TOGGLE_FULL_SCREEN_ACTION, selfContainedImpl.getApplicationActionMap());
			}
		}
	}

	private void setActionProxy(ApplicationActionMap actionMap, String actionName, ApplicationActionMap newActionMap) {
		ApplicationAction action = (ApplicationAction) actionMap.get(actionName);
		ApplicationAction proxyAction = (ApplicationAction) newActionMap.get(actionName);
		if (action != null) {
			// if action in there already, then set it's proxy
			action.setProxy(proxyAction);
		} else {
			// if action not in there, then add it to the map
			actionMap.put(actionName, proxyAction);
		}
	}

	private VideoMenuBar getMenuBar() {
		return menuBar;
	}

	private JScrollableDesktopPane getPane() {
		return pane;
	}

	/**
	 * Find an {@link SelfContained} in a given {@link Collection} for 
	 * which {@link SelfContained#getHolder()} equals {@link Component}.
	 * 
	 * @param <T> The concrete type of the {@link SelfContained}
	 * @param videoComponents The {@link Collection} to search
	 * @param component The current visible {@link Component}
	 * @return The found {@link SelfContained}
	 * 
	 * @see SelfContained#getHolder()
	 */
	public <T extends VideoComponent> T findVideoComponent(Iterable<T> videoComponents, final Component component) {
		T result = null;
		Iterator<T> iterator = videoComponents.iterator();
		while(iterator.hasNext() && result == null && component != null) {
			T videoComponent = iterator.next();
			if (videoComponent.isFocusable()) {
				IVideoComponent videoImpl = videoComponent.getVideoImpl();
				if (SwingUtilities.isDescendingFrom(((Containable) videoImpl).getComponent(), component)) {
					result = videoComponent;
				}
			}
		}
		return result;
	}

	public <T extends VideoComponent> T findVideoComponent(Iterable<T> videoComponents, final String title) {
		T result = null;
		Iterator<T> iterator = videoComponents.iterator();
		while(iterator.hasNext() && result == null) {
			T videoComponent = iterator.next();
			if (videoComponent.getVideoImpl().getTitle().equals(title)) {
				result = videoComponent;
			}
		}
		return result;
	}


	public void setTitle(Containable containable, String title) {
		AppInternalFrame internalFrame = getDecoratingComponent(AppInternalFrame.class, containable);
		if (internalFrame != null) {
			internalFrame.setTitle(title);
		}
	}

	public void toFront(VideoComponent videoComponent) {
		IVideoComponent videoImpl = videoComponent.getVideoImpl();
		if (videoImpl instanceof SelfContained) {
			SelfContained selfContainedImpl = (SelfContained) videoComponent.getVideoImpl();
			selfContainedImpl.toFront();
		} else if (videoImpl instanceof Containable) {
			SelfContained selfContained = getDecoratingComponent((Containable) videoImpl);
			if (selfContained != null) {
				selfContained.toFront();
			}
		}
	}

	/**
	 * A call to this method will ask the {@link RepaintManager} to redraw the whole {@link JDesktopPane}.
	 * This might be useful when mixing light- and heavyweight components over different screens.
	 */
	public void refreshScreen() {
		Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		JRootPane rootPane = SwingUtilities.getRootPane(activeWindow);
		RepaintManager repaintManager = RepaintManager.currentManager(rootPane);
		repaintManager.markCompletelyDirty(rootPane);
		rootPane.repaint();
	}

	public boolean isToggleFullScreenEnabled(VideoComponent videoComponent) {
		boolean result = getMonitorCount() > 1 && videoComponent.isIdle() && videoComponent.isActive();
		IVideoComponent videoImpl = videoComponent.getVideoImpl();
		if (videoImpl instanceof FullScreenSupport) {
			FullScreenSupport selfContainedImpl = (FullScreenSupport) videoComponent.getVideoImpl();
			result &= selfContainedImpl.isToggleFullScreenEnabled();
		} else if (videoImpl instanceof Containable) {
			FullScreenSupport fsVideoImpl = getDecoratingComponent(FullScreenSupport.class, (Containable) videoImpl);
			if (fsVideoImpl != null) {
				result &= fsVideoImpl.isToggleFullScreenEnabled();
			}
		}
		return result;
	}

	public boolean isVisible(VideoComponent videoComponent) {
		boolean result = false;
		IVideoComponent videoImpl = videoComponent.getVideoImpl();
		if (videoImpl instanceof SelfContained) {
			SelfContained selfContainedImpl = (SelfContained) videoComponent.getVideoImpl();
			result = selfContainedImpl.isVisible();
		} else if (videoImpl instanceof Containable) {
			SelfContained selfContained = getDecoratingComponent((Containable) videoImpl);
			if (selfContained != null) {
				result = selfContained.isVisible();
			}
		}
		return result;
	}

}
