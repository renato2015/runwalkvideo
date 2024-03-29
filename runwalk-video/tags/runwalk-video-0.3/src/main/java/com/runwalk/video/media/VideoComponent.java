package com.runwalk.video.media;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;

import javax.swing.ActionMap;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;

import com.runwalk.video.core.AppComponent;
import com.runwalk.video.core.Containable;
import com.runwalk.video.core.PropertyChangeSupport;
import com.runwalk.video.core.SelfContained;

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

	private WeakReference<ApplicationActionMap> actionMap;
	private volatile State state;
	private boolean overlayed;
	
	private String videoPath;
	
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
		if (!isIdle()) {
			getVideoImpl().stopRunning();
		} else {
			getVideoImpl().startRunning();
		}
	}
	
	/**
	 * Return <code>true</code> if the capturer implementation's 
	 * focus state can be tracked by the {@link KeyboardFocusManager}.
	 * @return <code>true</code> if the window's focus state can be tracked
	 */
	public boolean isFocusable() {
		return getVideoImpl() instanceof Containable && !getVideoImpl().isNativeWindowing();
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

	public void stopRunning() {
		getVideoImpl().stopRunning();
	}

	@Action(enabledProperty = IDLE)
	public void dispose() {
		if (getVideoImpl() != null) {			
			// dispose on the video implementation will dispose resources for the full screen frame
			getVideoImpl().dispose();
		}
		setVideoPath(null);
		// fire a PCE after disposing the implementation
		setState(State.DISPOSED);
	}

	public String getTitle() {
		return getVideoImpl().getTitle();
	}

	public boolean isActive() {
		return getVideoImpl().isActive();
	}

	public String getVideoPath() {
		return videoPath;
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}

	/**
	 * Merge the {@link ActionMap} of the implementation with the one of this instance.
	 * The result will be cached and wrapped using a WeakReference.
	 * 
	 * @return The merged ActionMap
	 */
	public synchronized ApplicationActionMap getApplicationActionMap() {
		if (actionMap == null && getVideoImpl() != null) {
			// get the action map of the abstractions
			ApplicationActionMap actionMap = getContext().getActionMap(VideoComponent.class, this);
			// get the action map of the implementations
			ApplicationActionMap implActionMap = null, parentImplActionMap = null;
			boolean isSelfContained = getVideoImpl() instanceof SelfContained;
			Class<?> startClass = isSelfContained ? SelfContained.class : Containable.class;
			implActionMap = parentImplActionMap = getApplicationActionMap(startClass, getVideoImpl());
			while (parentImplActionMap.getParent() != null && parentImplActionMap.getParent() != getContext().getActionMap()) {
				parentImplActionMap = (ApplicationActionMap) parentImplActionMap.getParent();
			}
			parentImplActionMap.setParent(actionMap);			
			this.actionMap = new WeakReference<ApplicationActionMap>(implActionMap);
		}
		return actionMap != null ? actionMap.get() : getContext().getActionMap(this);
	}

	public enum State {
		PLAYING, RECORDING, IDLE, DISPOSED, STOPPED
	}

}