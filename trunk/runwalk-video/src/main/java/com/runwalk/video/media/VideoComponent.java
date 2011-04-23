package com.runwalk.video.media;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;

import javax.swing.ActionMap;
import javax.swing.Timer;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;

import com.runwalk.video.entities.Recording;
import com.runwalk.video.ui.AppComponent;
import com.runwalk.video.ui.Containable;
import com.runwalk.video.ui.PropertyChangeSupport;
import com.runwalk.video.ui.SelfContained;

/**
 * This abstraction allows you to make easy reuse of the common video UI functionality  
 * used by the components that implement {@link IVideoPlayer} and {@link IVideoCapturer}.
 * 
 * @author Jeroen Peelaerts
 *
 */
@AppComponent
public abstract class VideoComponent implements PropertyChangeSupport {

	public static final String IDLE = "idle";
	public static final String STATE = "state";
	public static final String DISPOSED = "disposed";

	private Recording recording;
	private Timer timer;
	private WeakReference<ApplicationActionMap> actionMap;
	private State state;
	private boolean overlayed;

	/**
	 * This variable is used to determine the default monitor on which this component will be shown.
	 */
	private final int componentId;


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

	protected void setState(State state) {
		firePropertyChange(STATE, this.state, this.state = state);
		// full screen mode is enabled for this component if there are at least 2 monitors connected and the component is idle
		//GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		//GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		//setFullScreenEnabled(isIdle() && graphicsDevices.length > 1);
	}

	public State getState() {
		return state;
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
			getVideoImpl().dispose();
		}
		setRecording(null);
	}

	public String getTitle() {
		return getVideoImpl().getTitle();
	}

	public boolean isActive() {
		return getVideoImpl().isActive();
	}

	/**
	 * Merge the {@link ActionMap} of the implementation with the one of this instance..
	 */
	public synchronized ApplicationActionMap getApplicationActionMap() {
		if (actionMap == null && getVideoImpl() != null) {
			// get the action map of the abstractions
			ApplicationActionMap actionMap = getContext().getActionMap(VideoComponent.class, this);
			ApplicationActionMap insertionReference = actionMap;
			// get the action map of the implementations
			//Class<?> firstImplementor = getFirstImplementor(getVideoImpl().getClass(), SelfContained.class);
			ApplicationActionMap videoImplActionMap = null;
			if (getVideoImpl() instanceof SelfContained) {
				videoImplActionMap = getContext().getActionMap(SelfContained.class, getVideoImpl());
			} else {
				videoImplActionMap = getContext().getActionMap(Containable.class, getVideoImpl());
			}
			// the lastImplementor is the class whose hierarchy will be searched for IVideoComponent implementors
			Class<?> lastImplementor = getVideoImpl().getClass();
			// loop over all classes derived from VideoComponent
			while (videoImplActionMap != insertionReference) {
				// get the next implementor for IVideoComponent in the hierarchy of startClass
				lastImplementor = getLastImplementor(lastImplementor, IVideoComponent.class);
				// this loop is necessary if there are more classes in the implementors hierarchy than in that of the abstraction
				while(videoImplActionMap.getActionsClass() != lastImplementor && IVideoComponent.class.isAssignableFrom(lastImplementor)) {
					videoImplActionMap = (ApplicationActionMap) videoImplActionMap.getParent();
				}
				// now insert the found part of the implementation action map in the action map of the abstraction
				ApplicationActionMap oldParent = (ApplicationActionMap) insertionReference.getParent();
				insertionReference.setParent(videoImplActionMap);
				// save a reference to the parent of the implementation's action map
				ApplicationActionMap tail = (ApplicationActionMap) videoImplActionMap.getParent();
				// if the implementation action map's class implements IAppComponent, then we need to add its actionmap
				videoImplActionMap.setParent(oldParent);
				// the next insertion point of the abstraction's action map will be it's parent before the insertion
				if (IVideoComponent.class.isAssignableFrom(videoImplActionMap.getActionsClass())) {
					insertionReference = oldParent;
					lastImplementor = lastImplementor.getSuperclass();
				}
				// set the implementation's action map to the parent of the action map that was inserted into the abstractions' action map
				videoImplActionMap = tail;
			}
			this.actionMap = new WeakReference<ApplicationActionMap>(actionMap);
		}
		return actionMap != null ? actionMap.get() : getContext().getActionMap(this);
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
		boolean recurse = !implementsInterface(theClass, interf) && theClass.getSuperclass() != null;
		// if no result here then recurse
		return recurse ? getLastImplementor(theClass.getSuperclass(), interf) : theClass;
	}

	public enum State {
		PLAYING, RECORDING, IDLE, DISPOSED, STOPPED
	}

}