package com.runwalk.video.ui;

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.SwingUtilities;

import org.jdesktop.application.ApplicationAction;
import org.jdesktop.application.ApplicationActionMap;

import com.runwalk.video.media.IVideoComponent;
import com.runwalk.video.media.IVideoPlayer;
import com.runwalk.video.media.VideoCapturer;
import com.runwalk.video.media.VideoComponent;
import com.runwalk.video.media.VideoComponent.State;
import com.runwalk.video.media.VideoPlayer;
import com.tomtessier.scrollabledesktop.JScrollableDesktopPane;

public class WindowManager implements PropertyChangeListener, WindowConstants {

	private VideoMenuBar menuBar;
	private JScrollableDesktopPane pane;

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
	public static int getDefaultMonitorId(int monitorCount, int componentId) {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		GraphicsDevice defaultGraphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		// assign default monitor number to initial result
		int defaultMonitorId = Arrays.asList(graphicsDevices).indexOf(defaultGraphicsDevice);
		int result = defaultMonitorId;
		int availableScreenCount = monitorCount - 1;
		if (monitorCount > 1) {
			// set to monitor on which the main window is not showing
			result = availableScreenCount - defaultMonitorId;
		}
		//TODO add support for three monitors
		return result;
	}
	
	public static int getDefaultMonitorId(VideoComponent videoComponent) {
		return getDefaultMonitorId(getMonitorCount(), videoComponent.getComponentId());
	}
	
	public static int getDefaultMonitorId() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String monitorIdString = graphicsEnvironment.getDefaultScreenDevice().getIDstring();
		monitorIdString = monitorIdString.substring(monitorIdString.length() - 1);
		return Integer.parseInt(monitorIdString);
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
	
	public void addWindow(VideoPlayer videoPlayer) {
		IVideoPlayer videoImpl = videoPlayer.getVideoImpl();
		if (videoImpl instanceof SelfContained) {
			int monitorId = getDefaultMonitorId(videoPlayer);
			((SelfContained) videoImpl).setMonitorId(monitorId);			
		}
		addWindow((VideoComponent) videoPlayer);
	}

	public void addWindow(VideoComponent videoComponent) {
		// FIXME can be null here
		IVideoComponent videoImpl = videoComponent.getVideoImpl();
		boolean isContainable = videoImpl instanceof Containable;
		boolean isSelfContained = videoImpl instanceof SelfContained;
		boolean isFullScreenSupported = videoImpl instanceof FullScreenSupport;
		if (isSelfContained) {
			SelfContained selfContainedImpl = (SelfContained) videoImpl;
			if (isFullScreenSupported) {
				FullScreenSupport fsVideoImpl = (FullScreenSupport) videoImpl;
				boolean toggleFullScreenEnabled = fsVideoImpl.isToggleFullScreenEnabled();
				boolean fullScreen = fsVideoImpl.isFullScreen();
				if (toggleFullScreenEnabled) {
					Integer monitorId = fsVideoImpl.getMonitorId();
					// go fullscreen if monitorId not on the default screen
					if (monitorId != null && monitorId != getDefaultMonitorId()) {
						fsVideoImpl.toggleFullScreen();
						addWindow(selfContainedImpl, videoComponent.getApplicationActionMap());
					} else if (isContainable) {
						addWindow((Containable) videoImpl, videoComponent.getApplicationActionMap());
					}
				} else if (isContainable && !fullScreen) {
					addWindow((Containable) videoImpl, videoComponent.getApplicationActionMap());
				}
			}
		} else if (isContainable) {
			addWindow((Containable) videoImpl, videoComponent.getApplicationActionMap());
		}
	}

	public void addWindow(SelfContained selfContained) {
		addWindow(selfContained, selfContained.getApplicationActionMap());
	}

	public void addWindow(Containable containable) {
		addWindow(containable, containable.getApplicationActionMap());
	}
	
	private void addWindow(Containable containable, ApplicationActionMap actionMap) {
		AppInternalFrame selfContainedImpl = createInternalFrame(containable.getComponent(), containable.getTitle());
		// reeds bestaande actions in action map delegeren naar die van selfContainedImpl
		setActionProxy(actionMap, TOGGLE_VISIBILITY_ACTION, selfContainedImpl.getApplicationActionMap());
		setActionProxy(actionMap, TOGGLE_FULL_SCREEN_ACTION, selfContainedImpl.getApplicationActionMap());
		addWindow(selfContainedImpl, actionMap);
	}

	//TODO add windows here to toggle between fullscreen and windowed mode
	private void addWindow(final SelfContained selfContainedImpl, ActionMap actionMap) {
		//TODO enforce constraint here: should only be called once for each window
		if (selfContainedImpl != null) {
			selfContainedImpl.addPropertyChangeListener(this);		
			getMenuBar().addMenu(selfContainedImpl.getTitle(), actionMap);
			showWindow(selfContainedImpl);
		}
	}
	
	public void setWindowVisibility(SelfContained selfContained, boolean visible) {
		selfContained.setVisible(visible);
	}

	public void showWindow(SelfContained selfContained) {
		setWindowVisibility(selfContained, true);
	}

	private <T extends SelfContained> T getDecoratingComponent(Class<T> theClass, Containable containable) {
		T result = null;
		Component component = containable.getComponent();
		if (component != null) {
			result = theClass.cast(SwingUtilities.getAncestorOfClass(theClass, component));
		}
		return result;
	}

	private SelfContained getDecoratingComponent(Containable containable) {
		return getDecoratingComponent(SelfContained.class, containable);
	}

	private AppInternalFrame createInternalFrame(Component component, String title) {
		AppInternalFrame internalFrame = new AppInternalFrame(title, true);
		internalFrame.add(component);
		internalFrame.pack();
		getPane().add(internalFrame);
		// TODO toggle enabled state of button in docking framework
		/*BeanProperty<JComponent, Boolean> enabled = BeanProperty.create("associatedButton.enabled");
		ELProperty<AppWindowWrapper, Boolean> fullScreen = ELProperty.create("{!fullScreen}");
		Binding<? extends AppWindowWrapper, Boolean, ? extends JComponent, Boolean> enabledBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ, this, fullScreen, internalFrame, enabled);*/
		/*enabledBinding.addBindingListener(new AbstractBindingListener() {

			@Override
			public void bindingBecameBound(Binding binding) {
				// TODO Auto-generated method stub
				super.bindingBecameBound(binding);
			}

			@Override
			public void syncFailed(Binding binding, SyncFailure failure) {
				// TODO Auto-generated method stub
				super.syncFailed(binding, failure);
			}

			@Override
			public void synced(Binding binding) {
				// TODO Auto-generated method stub
				super.synced(binding);
			}

		});*/
		//enabledBinding.bind();
		return internalFrame;
	}

	/*	private Integer checkMonitorId(Integer monitorId, int monitorCount) {
		// check preconditions
		if (monitorId != null && monitorId < monitorCount) {
			// use monitor set by user
			Logger.getLogger(WindowManager.class).log(Level.DEBUG, "Monitor number " + monitorId + " selected for " + getTitle() + ".");
		} else {
			// use default monitor, because it wasn't set or found to be invalid
			monitorId = getDefaultMonitorId(0, getComponentId());
			Logger.getLogger(WindowManager.class).log(Level.WARN, "Default monitor number " + monitorId + " selected for " + getTitle() + ".");
		}
		return monitorId;
	}*/

	public void propertyChange(PropertyChangeEvent evt) {
		// disposing windows is handled by listening to the components' eventsget
		// FIXME disposing should be done by... the video impl itself??
		if (VideoComponent.STATE.equals(evt.getPropertyName())) {
			VideoComponent.State newState = (State) evt.getNewValue();
			if (VideoComponent.State.DISPOSED.equals(newState)) {
				VideoComponent videoComponent = (VideoComponent) evt.getSource();
				getMenuBar().removeMenu(videoComponent.getTitle());
				videoComponent.removePropertyChangeListener(this);
				IVideoComponent videoImpl = videoComponent.getVideoImpl();
				// if the implementation implements both SelfContained and Containable
				if (videoImpl instanceof SelfContained) {
					((SelfContained) videoImpl).removePropertyChangeListener(this);
					
				} else if (videoImpl instanceof Containable) {
					// if Containable and not SelfContained then dispose the wrapping component
					SelfContained selfContainedImpl = getDecoratingComponent((Containable) videoImpl);
					selfContainedImpl.removePropertyChangeListener(this);
					//selfContainedImpl.dispose();
				}
			}
		} else if (WindowConstants.FULL_SCREEN.equals(evt.getPropertyName())) {
			if (evt.getSource() instanceof Containable) {
				Containable containable = (Containable) evt.getSource();
				// go back to windowed mode
				Boolean newValue = (Boolean) evt.getNewValue();
				if (newValue == Boolean.FALSE) {
					AppInternalFrame selfContainedImpl = createInternalFrame(containable.getComponent(), containable.getTitle());
					// handle SelfContained action proxy's..
					setActionProxy(containable.getApplicationActionMap(), TOGGLE_VISIBILITY_ACTION, selfContainedImpl.getApplicationActionMap());
					setActionProxy(containable.getApplicationActionMap(), TOGGLE_FULL_SCREEN_ACTION, selfContainedImpl.getApplicationActionMap());
					showWindow(selfContainedImpl);
				}
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
			IVideoComponent videoImpl = videoComponent.getVideoImpl();
			if (videoImpl instanceof Containable) {
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

	public boolean isToggleFullScreenEnabled(VideoComponent videoComponent) {
		boolean result = false;
		IVideoComponent videoImpl = videoComponent.getVideoImpl();
		if (videoImpl instanceof FullScreenSupport) {
			FullScreenSupport selfContainedImpl = (FullScreenSupport) videoComponent.getVideoImpl();
			result = selfContainedImpl.isToggleFullScreenEnabled();
		} else if (videoImpl instanceof Containable) {
			FullScreenSupport fsVideoImpl = getDecoratingComponent(FullScreenSupport.class, (Containable) videoImpl);
			if (fsVideoImpl != null) {
				result = fsVideoImpl.isToggleFullScreenEnabled();
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
