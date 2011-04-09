package com.runwalk.video.ui;

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ActionMap;
import javax.swing.SwingUtilities;

import com.runwalk.video.media.IVideoComponent;
import com.runwalk.video.media.VideoCapturer;
import com.runwalk.video.media.VideoComponent;
import com.runwalk.video.media.VideoComponent.State;
import com.runwalk.video.media.VideoPlayer;
import com.tomtessier.scrollabledesktop.JScrollableDesktopPane;

public class WindowManager implements PropertyChangeListener {

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

	public WindowManager(VideoMenuBar menuBar, JScrollableDesktopPane pane) {
		this.menuBar = menuBar;
		this.pane = pane;
	}

	public void addWindow(VideoComponent videoComponent) {
		IVideoComponent videoImpl = videoComponent.getVideoImpl();
		boolean isContainable = videoImpl instanceof Containable;
		boolean isSelfContained = videoImpl instanceof SelfContained;
		if (isSelfContained) {
			SelfContained selfContainedImpl = (SelfContained) videoImpl;
			boolean fullScreen = selfContainedImpl.isFullScreen();
			boolean toggleFullScreenEnabled = selfContainedImpl.isToggleFullScreenEnabled();
			if (!fullScreen && toggleFullScreenEnabled) {
				// go fullscreen by default
				int monitorId = getDefaultMonitorId(2, videoComponent.getComponentId());
				selfContainedImpl.setFullScreen(true, monitorId);
				selfContainedImpl.addPropertyChangeListener(this);
			} else if (isContainable && !fullScreen && !toggleFullScreenEnabled) {
				Component component = ((Containable) videoImpl).getComponent();
				selfContainedImpl = createInternalFrame(component, videoComponent.getTitle());
			}
			selfContainedImpl.addPropertyChangeListener(this);
			//TODO add else condition, if not fullscreen and toggle unavailable t
			addWindow(selfContainedImpl, videoComponent.getApplicationActionMap());
		} else if (isContainable) {
			Component component = ((Containable) videoImpl).getComponent();
			SelfContained selfContainedImpl = createInternalFrame(component, videoComponent.getTitle());
			selfContainedImpl.addPropertyChangeListener(this);
		}
	}

	public void addWindow(SelfContained selfContained) {
		addWindow(selfContained, selfContained.getApplicationActionMap());
	}
	
	public void addWindow(Containable containable) {
		createInternalFrame(containable.getComponent(), containable.getTitle());
	}

	//TODO add windows here to toggle between fullscreen and windowed mode
	private void addWindow(SelfContained appComponent, ActionMap actionMap) {
		//TODO enforce constraint here: should only be called once for each window
		if (appComponent != null) {
			getMenuBar().addMenu(appComponent.getTitle(), actionMap);
			showWindow(appComponent);
		}
	}
	
	public void setWindowVisibility(Containable containable, boolean visible) {
		SelfContained component = getDecoratingComponent(containable);  
		if (component != null) {
			component.setVisible(visible);
		}
	}

	public void setWindowVisibility(SelfContained selfContained, boolean visible) {
		selfContained.setVisible(visible);
	}

	public void showWindow(SelfContained selfContained) {
		setWindowVisibility(selfContained, true);
	}
	
	public void showWindow(Containable containable) {
		setWindowVisibility(containable, true);
	}

	public void hideWindow(SelfContained selfContained) {
		setWindowVisibility(selfContained, false);
	}
	
	public void hideWindow(Containable containable) {
		setWindowVisibility(containable, false);
	}

	private SelfContained getDecoratingComponent(Containable containable) {
		SelfContained result = null;
		Component component = containable.getComponent();
		if (component != null) {
			component = SwingUtilities.getAncestorOfClass(SelfContained.class, component);
		}
		return result;
	}

	private AppInternalFrame createInternalFrame(Component component, String title) {
		AppInternalFrame internalFrame = new AppInternalFrame(title, false);
		internalFrame.add(component);
		internalFrame.pack();
		getPane().add(internalFrame);
		// FIXME add component listener to internal frame to detect closing
		//internalFrame.addComponentListener(this);
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
		if (VideoComponent.STATE.equals(evt.getPropertyName())) {
			VideoComponent.State newState = (State) evt.getNewValue();
			if (VideoComponent.State.DISPOSED.equals(newState)) {
			/*	VideoComponent videoComponent = (SelfContained) evt.getSource();
				getMenuBar().removeMenu(appComponent.getTitle());
				appComponent.removePropertyChangeListener(this);
				if (decoratedComponent != null) {
					((JInternalFrame) decoratedComponent).dispose();
				}*/
			}
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
	 * @param windowWrappers The {@link Collection} to look in 
	 * @param component The current visible {@link Component}
	 * @return The found {@link SelfContained}
	 * 
	 * @see SelfContained#getHolder()
	 */
	public <T extends VideoComponent> T getWindowWrapper(Iterable<T> windowWrappers, final Component component) {
		T result = null;
		Iterator<T> iterator = windowWrappers.iterator();
		while(iterator.hasNext() && result == null) {
			T next = iterator.next();
			if (next.getTitle() == component.getName()) {
				result = next;
			}
		}
		return result;
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
		if (videoImpl instanceof SelfContained) {
			SelfContained selfContainedImpl = (SelfContained) videoComponent.getVideoImpl();
			result = selfContainedImpl.isToggleFullScreenEnabled();
		} else if (videoImpl instanceof Containable) {
			SelfContained selfContained = getDecoratingComponent((Containable) videoImpl);
			if (selfContained != null) {
				result = selfContained.isToggleFullScreenEnabled();
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
