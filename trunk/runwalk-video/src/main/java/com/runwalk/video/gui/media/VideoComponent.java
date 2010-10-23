package com.runwalk.video.gui.media;

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
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.Timer;

import org.apache.log4j.Level;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;

import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.AppInternalFrame;
import com.runwalk.video.gui.AppWindowWrapper;
import com.runwalk.video.gui.PropertyChangeSupport;
import com.runwalk.video.gui.tasks.AbstractTask;
import com.runwalk.video.util.AppUtil;

/**
 * This abstraction allows you to make easy reuse of the vendor independent logic 
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
	public static int getDefaultMonitorId(int graphicsDevicesCount, int componentId) {
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
		int defaultMonitorId = getDefaultMonitorId(monitorCount, getComponentId());
		// check preconditions
		if (monitorId != null && monitorId < monitorCount) {
			// use monitor set by user
			getLogger().log(Level.INFO, "Monitor number " + monitorId + " selected for " + getTitle() + ".");
		} else {
			// use default monitor, because it wasn't set or found to be invalid
			monitorId = defaultMonitorId;
			getLogger().log(Level.WARN, "Default monitor number " + monitorId + " selected for " + getTitle() + ".");
		}
		return monitorId;
	}
	
	protected void showComponent() {
		showComponent(true, null);
	}
	
	protected void showComponent(boolean fullScreen, Integer monitorId) {
		// get the graphicsdevice corresponding with the given monitor id
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		monitorId = checkMonitorId(monitorId, graphicsDevices.length);
		GraphicsDevice graphicsDevice = graphicsDevices[monitorId];
		// get a reference to the previously visible component
		Container oldContainer = getHolder();
		// go fullscreen if the selected monitor is not the primary one (index 0)
		fullScreen = fullScreen && monitorId > 0;
		if (fullScreen) {
			boolean frameInitialized = getFullScreenFrame() != null;
			getVideoImpl().setFullScreen(graphicsDevice, true);
			if (!frameInitialized) {
				getFullScreenFrame().addComponentListener(this);
			}
		} else {
			getVideoImpl().setFullScreen(graphicsDevice, false);
			if (getInternalFrame() == null) {
				createInternalFrame();
			}
		}
		if (oldContainer != null) {
			oldContainer.setVisible(false);
		}
		setFullScreen(fullScreen);
		setComponentTitle(getTitle());
		setVisible(true);
		getApplication().createOrShowComponent(this);
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
		if (isFullScreen()) {
			getFullScreenFrame().toFront();
		} else {
			getInternalFrame().toFront();
		}
	}

	public boolean isActive() {
		return getHolder() != null && getHolder().isVisible() && getVideoImpl().isActive();
	}

	/**
	 * Merge the {@link ActionMap} of the implementation with the one of this instance..
	 */
	public ActionMap getApplicationActionMap() {
		if (actionMap == null) {
			ActionMap actionMap = getContext().getActionMap(AppWindowWrapper.class, this);
			if (getVideoImpl() == null) {
				return actionMap;
			}
			Class<?> videoComponentImpl = getActionSuperClass(getVideoImpl().getClass());
			ActionMap videoImplActionMap = getContext().getActionMap(videoComponentImpl, getVideoImpl());
			this.actionMap = new WeakReference<ActionMap>(AppUtil.mergeActionMaps(actionMap, videoImplActionMap));
		}
		return actionMap.get();
	}

	private Class<?> getActionSuperClass(Class<?> theClass) {
		List<Class<?>> interfaces = Arrays.asList(theClass.getInterfaces());
		if (!interfaces.contains(IVideoComponent.class)) {
			return getActionSuperClass(theClass.getSuperclass());
		}
		return theClass;
	}

	public void componentShown(ComponentEvent e) {
		firePropertyChange(VISIBLE, this.visible, this.visible = e.getComponent().isVisible());
	}

	public void componentHidden(ComponentEvent e) {
		firePropertyChange(VISIBLE, this.visible, this.visible = e.getComponent().isVisible());
	}

	public void componentResized(ComponentEvent e) { }

	public void componentMoved(ComponentEvent e) { }

	public enum State {
		PLAYING, RECORDING, IDLE, DISPOSED, STOPPED
	}

}