package com.runwalk.video.media;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.Timer;

import org.apache.log4j.Level;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.Task;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;

import com.google.common.collect.Lists;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.AppInternalFrame;
import com.runwalk.video.gui.AppWindowWrapper;
import com.runwalk.video.gui.PropertyChangeSupport;
import com.runwalk.video.gui.tasks.AbstractTask;

/**
 * This abstraction allows you to make easy reuse of the common video UI functionality  
 * used by the components that implement {@link IVideoPlayer} and {@link IVideoCapturer}.
 * 
 * @author Jeroen Peelaerts
 *
 */
public abstract class VideoComponent implements PropertyChangeSupport, AppWindowWrapper, ComponentListener {

	public static final String FULL_SCREEN = "fullScreen";
	public static final String IDLE = "idle";
	public static final String FULL_SCREEN_ENABLED = "fullScreenEnabled";
	public static final String STATE = "state";
	public static final String MONITOR_ID = "monitorId";
	public static final String DISPOSED = "disposed";

	private Recording recording;
	private AppInternalFrame internalFrame;
	private Timer timer;
	private boolean fullScreen = false;
	private WeakReference<ActionMap> actionMap;
	private State state;
	private Integer monitorId;
	private boolean fullScreenEnabled;
	private boolean overlayed;
	private boolean visible = true;
	private Binding<? extends AppWindowWrapper, Boolean, ? extends JComponent, Boolean> enabledBinding;

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

	protected VideoComponent(int componentId) {
		this.componentId = componentId;
		setIdle(true);
	}

	public abstract IVideoComponent getVideoImpl();

	/**
	 * This method simply invokes {@link #startRunning()} if the video component is stopped 
	 * or {@link #stopRunning()} if the component is running at invocation time.
	 */
	@Action(selectedProperty = IDLE)
	public void togglePreview() {
		if (isIdle()) {
			getVideoImpl().stopRunning();
		} else {
			getVideoImpl().startRunning();
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

	public Frame getFullScreenFrame() {
		return getVideoImpl().getFullscreenFrame();
	}

	public AppInternalFrame getInternalFrame() {
		return internalFrame;
	}

	/**
	 * Returns an unique id for this concrete {@link VideoComponent} type.
	 * Implementations can be numbered independently from each other.
	 * Most of the time a static counter will be used at creation time, that 
	 * is incremented in the subclass.
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
		if (isFullScreen()) {
			getFullScreenFrame().setTitle(title);
			getFullScreenFrame().setName(title);
		} else {
			getInternalFrame().setName(title);
			getInternalFrame().setTitle(title);
		}
	}

	public Container getHolder() {
		Container container = null;
		if (isFullScreen()) {
			container = getFullScreenFrame();
		} else {
			container = getInternalFrame();
		}
		return container;
	}

	protected void setMonitorId(int monitorId) {
		this.monitorId = monitorId;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		firePropertyChange(FULL_SCREEN, this.fullScreen, this.fullScreen = fullScreen);
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		firePropertyChange(VISIBLE, this.visible, this.visible = visible);
	}

	public void toggleVisibility() {
		getHolder().setVisible(isVisible());
	}

	public BufferedImage getImage() {
		return getVideoImpl().getImage();
	}

	public void setBlackOverlayImage() {
		Dimension dimension = getVideoImpl().getDimension();
		if (dimension != null) {
			final BufferedImage newOverlay = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_4BYTE_ABGR); 
			setOverlayImage(newOverlay, Color.white);
		}
	}

	public void setOverlayImage(BufferedImage image, Color alphaColor) {
		getVideoImpl().setOverlayImage(image, alphaColor);
		setOverlayed(true);
	}

	private void setOverlayed(boolean overlayed) {
		this.overlayed = overlayed;
	}

	public boolean isOverlayed() {
		return overlayed;
	}

	private Integer checkMonitorId(Integer monitorId, int monitorCount) {
		// check preconditions
		if (monitorId != null && monitorId < monitorCount) {
			// use monitor set by user
			getLogger().log(Level.DEBUG, "Monitor number " + monitorId + " selected for " + getTitle() + ".");
		} else {
			// use default monitor, because it wasn't set or found to be invalid
			monitorId = getDefaultMonitorId(0, getComponentId());
			getLogger().log(Level.WARN, "Default monitor number " + monitorId + " selected for " + getTitle() + ".");
		}
		return monitorId;
	}

	protected void showComponent() {
		// TODO monitorId property is set by creation factory.. maybe it can be passed as an argument, too?
		showComponent(true, this.monitorId);
	}

	protected void showComponent(boolean fullScreen, Integer monitorId) {
		// get the graphicsdevice corresponding with the given monitor id
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		GraphicsDevice defaultGraphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		monitorId = checkMonitorId(monitorId, graphicsDevices.length);
		GraphicsDevice graphicsDevice = graphicsDevices[monitorId];
		// get a reference to the previously visible component
		Container oldContainer = getHolder();
		// go fullscreen if the selected monitor is not the default one
		fullScreen = fullScreen && graphicsDevice != defaultGraphicsDevice;
		if (fullScreen) {
			boolean frameInitialized = getFullScreenFrame() != null;
			getVideoImpl().setFullScreen(graphicsDevice, true);
			if (!frameInitialized) {
				getFullScreenFrame().addComponentListener(this);
			}
		} else {
			getVideoImpl().setFullScreen(graphicsDevice, false);
			if (getInternalFrame() == null && getVideoImpl().getComponent() != null) {
				createInternalFrame();
			}
		}
		if (oldContainer != null) {
			oldContainer.setVisible(false);
		}
		setFullScreen(fullScreen);
		if (getHolder() != null) {
			setComponentTitle(getTitle());
			setVisible(true);
			getApplication().createOrShowComponent(this);
		}
	}

	private void createInternalFrame() {
		internalFrame = new AppInternalFrame(getTitle(), false);
		getInternalFrame().add(getVideoImpl().getComponent());
		getInternalFrame().addComponentListener(this);
		BeanProperty<JComponent, Boolean> enabled = BeanProperty.create("associatedButton.enabled");
		ELProperty<AppWindowWrapper, Boolean> fullScreen = ELProperty.create("{!fullScreen}");
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, fullScreen, getInternalFrame(), enabled);
		enabledBinding.addBindingListener(new AbstractBindingListener() {

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

		});
		enabledBinding.bind();
	}

	@org.jdesktop.application.Action(enabledProperty = FULL_SCREEN_ENABLED, selectedProperty = FULL_SCREEN, block = BlockingScope.ACTION)
	public Task<Void, Void> toggleFullScreen() {
		return new AbstractTask<Void, Void>("toggleFullScreen") {

			protected Void doInBackground() throws Exception {
				// go fullscreen if component is displaying on the primary device, otherwise apply windowed mode
				monitorId = monitorId != null && monitorId == 0 ? null : 0;
				showComponent(isFullScreen(), monitorId);
				return null;
			}

		};
	}

	protected void setState(State state) {
		firePropertyChange(STATE, this.state, this.state = state);
		// full screen mode is enabled for this component if there are at least 2 monitors connected and the component is idle
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		setFullScreenEnabled(isIdle() && graphicsDevices.length > 1);
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

	public void setIdle(boolean idle) {
		boolean wasIdle = isIdle();
		setState(idle ? State.IDLE : State.STOPPED);
		firePropertyChange(IDLE, wasIdle, isIdle());
	}

	public boolean isStopped() {
		return getState() == State.STOPPED;
	}

	private void maybeRemoveComponentListener(Component comp, ComponentListener l) {
		if (comp != null) {
			comp.addComponentListener(l);
		}
	}

	public void stopRunning() {
		getVideoImpl().stopRunning();
	}

	@Action(enabledProperty = IDLE)
	public void dispose() {
		// fire event before removing listeners
		setState(State.DISPOSED);
		// no componentlisteners left here??
		// no propertyChangeListener left here??
		if (getVideoImpl() != null) {			
			// dispose on the video implementation will dispose resources for the full screen frame
			maybeRemoveComponentListener(getFullScreenFrame(), this);
			getVideoImpl().dispose();
			if (getInternalFrame() != null) {
				enabledBinding.unbind();
				getInternalFrame().removeComponentListener(this);
				getInternalFrame().dispose();
			}
		}
		setRecording(null);
	}

	public String getTitle() {
		return getVideoImpl().getTitle();
	}

	public void toFront() {
		// FIXME clean this up
		if (getHolder() != null) {
			if (isFullScreen()) {
				getFullScreenFrame().toFront();
			} else {
				getInternalFrame().toFront();
			}
		}
	}

	public boolean isActive() {
		return getHolder() != null && getHolder().isVisible() && getVideoImpl().isActive();
	}

	/**
	 * Merge the {@link ActionMap} of the implementation with the one of this instance..
	 */
	public ActionMap getApplicationActionMap() {
		if (actionMap == null && getVideoImpl() != null) {
			// get the action map of the abstractions
			ActionMap actionMap = getContext().getActionMap(AppWindowWrapper.class, this);
			ActionMap insertionReference = actionMap;
			// get the action map of the implementations
			Class<?> firstImplementor = getFirstImplementor(getVideoImpl().getClass(), IVideoComponent.class);
			ApplicationActionMap videoImplActionMap = getContext().getActionMap(firstImplementor, getVideoImpl());
			// the lastImplementor is the class whose hierarchy will be searched for IVideoComponent implementors
			Class<?> lastImplementor = getVideoImpl().getClass();
			Class<?> abstractionClass = getClass();
			// loop over all classes derived from VideoComponent
			while (VideoComponent.class.isAssignableFrom(abstractionClass)) {
				// get the next implementor for IVideoComponent in the hierarchy of startClass
				lastImplementor = getLastImplementor(lastImplementor, IVideoComponent.class);
				// this loop is necessary if there are more classes in the implementors hierarchy than in that of the abstraction
				while(videoImplActionMap.getActionsClass() != lastImplementor) {
					videoImplActionMap = (ApplicationActionMap) videoImplActionMap.getParent();
				}
				// now insert the found part of the implementation action map in the action map of the abstraction
				ActionMap oldParent = insertionReference.getParent();
				insertionReference.setParent(videoImplActionMap);
				// save a reference to the parent of the implementation's action map
				ApplicationActionMap tail = (ApplicationActionMap) videoImplActionMap.getParent();
				videoImplActionMap.setParent(oldParent);
				// set the implementation's action map to the parent of the action map that was inserted into the abstractions' action map
				videoImplActionMap = tail;
				// the next insertion point of the abstraction's action map will be it's parent before the insertion
				insertionReference = oldParent;
				lastImplementor = lastImplementor.getSuperclass();
				abstractionClass = abstractionClass.getSuperclass();
			}
			this.actionMap = new WeakReference<ActionMap>(actionMap);
		}
		return actionMap != null ? actionMap.get() : getContext().getActionMap(this);
	}

	/**
	 * Search a class hierarchy from bottom to top and return the first {@link Class} that implements the given interface.
	 * 
	 * @param theClass The {@link Class} whose hierarchy will be searched
	 * @param interf The interface
	 * @return The first {@link Class} implementing the given interface
	 */
	private Class<?> getFirstImplementor(Class<?> theClass, Class<?> interf) {
		List<Class<?>> allClasses = Lists.newArrayList();
		while(theClass != null) {
			allClasses.add(theClass);
			theClass = theClass.getSuperclass();
		}
		Collections.reverse(allClasses);
		for (Class<?> firstClass : allClasses) {
			if (implementsInterface(firstClass, interf)) {
				return firstClass;
			}
		}
		return null;
	}

	/**
	 * Check whether a {@link Class} implements the given interface.
	 * 
	 * @param theClass The class
	 * @param interf The interface
	 * @return <code>true</code> if the {@link Class} implements the interface
	 */
	private boolean implementsInterface(Class<?> theClass, Class<?> interf) {
		for (Class<?> implementedInterface : theClass.getInterfaces()) {
			if (interf.isAssignableFrom(implementedInterface)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Search a class hierarchy from top to bottom and return the first {@link Class} that implements the given interface.
	 * 
	 * @param theClass The {@link Class} whose hierarchy will be searched
	 * @param interf The interface
	 * @return The first {@link Class} implementing the given interface
	 */
	private Class<?> getLastImplementor(Class<?> theClass, Class<?> interf) {
		boolean recurse = !implementsInterface(theClass, interf);
		// if no result here then recurse
		return recurse ? getLastImplementor(theClass.getSuperclass(), interf) : theClass;
	}

	public void componentShown(ComponentEvent e) {
		setVisible(e.getComponent().isVisible());
	}

	public void componentHidden(ComponentEvent e) {
		setVisible(e.getComponent().isVisible());
	}

	public void componentResized(ComponentEvent e) { }

	public void componentMoved(ComponentEvent e) { }

	public enum State {
		PLAYING, RECORDING, IDLE, DISPOSED, STOPPED
	}

}