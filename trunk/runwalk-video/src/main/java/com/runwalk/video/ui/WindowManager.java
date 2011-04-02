package com.runwalk.video.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

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
	
	private boolean isDecoratedWindow(Component component) { 
		Container ancestorOfClass = SwingUtilities.getAncestorOfClass(JInternalFrame.class, component);
		return Window.class.isAssignableFrom(component.getClass()) || ancestorOfClass == null;
	}
	
	public void addWindow(VideoComponent videoComponent) {
		// go fullscreen by default
		addWindow((AppWindowWrapper) videoComponent);
		int monitorId = getDefaultMonitorId(2, videoComponent.getComponentId());
		videoComponent.setFullScreen(true, monitorId);
	}

	//TODO add windows here to toggle between fullscreen and windowed mode
	public void addWindow(AppWindowWrapper appComponent) {
		//TODO enforce constraint here: should only be called once for each window
		if (appComponent != null) {
			Component component = appComponent.getHolder();
			if (component != null) {
				// add a PCE so this object can mediate events between the menu bar and video component
				component.addPropertyChangeListener(this);
				// check whether container implements the 
				// if the given component is not a subclass of Window then we have to put it in a viewable container ourselves
				JInternalFrame internalFrame = null;
				if (!isDecoratedWindow(component)) {
					internalFrame = createInternalFrame(component, appComponent.getTitle());
				} else if (component instanceof JInternalFrame) {
					internalFrame = (JInternalFrame) component;
					internalFrame.pack();
					getPane().add(internalFrame);
				}
			}
			getMenuBar().addMenu(appComponent.getTitle(), appComponent.getApplicationActionMap());
			showWindow(appComponent);
		}
	}

	public void setWindowVisibility(AppWindowWrapper appComponent, boolean visible) {
		Component component = getDecoratingComponent(appComponent);  
		if (component != null) {
			component.setVisible(visible);
		}
		appComponent.setVisible(visible);
	}
	
	public void showWindow(AppWindowWrapper appComponent) {
		setWindowVisibility(appComponent, true);
	}

	public void hideWindow(AppWindowWrapper appComponent) {
		setWindowVisibility(appComponent, false);
	}
	
	/**
	 * @param appComponent
	 */
	private Component getDecoratingComponent(AppWindowWrapper appComponent) {
		Component component = appComponent.getHolder();
		if (component != null) {
			component = SwingUtilities.getAncestorOfClass(JInternalFrame.class, appComponent.getHolder());
		}
		return component;
	}

	private AppInternalFrame createInternalFrame(Component component, String title) {
		AppInternalFrame internalFrame = new AppInternalFrame(title, false);
		
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
				AppWindowWrapper appComponent = (AppWindowWrapper) evt.getSource();
				getMenuBar().removeMenu(appComponent.getTitle());
				appComponent.removePropertyChangeListener(this);
				Component decoratedComponent = getDecoratingComponent(appComponent);
				if (decoratedComponent != null) {
					((JInternalFrame) decoratedComponent).dispose();
				}
			}
		}
	}

	private VideoMenuBar getMenuBar() {
		return menuBar;
	}

	private JScrollableDesktopPane getPane() {
		return pane;
	}

}
