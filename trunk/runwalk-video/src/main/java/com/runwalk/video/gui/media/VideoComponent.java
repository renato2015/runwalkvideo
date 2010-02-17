package com.runwalk.video.gui.media;

import java.awt.Container;

import java.awt.Frame;
import java.beans.PropertyChangeListener;

import javax.swing.JInternalFrame;
import javax.swing.Timer;

import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.ComponentDecorator;
import com.runwalk.video.gui.MyInternalFrame;

/**
 * This abstraction allows you to make easy reuse of the vendor independent logic 
 * used by the components that implement {@link IVideoPlayer} and {@link IVideoCapturer}.
 * 
 * @author Jeroen P.
 *
 */
public abstract class VideoComponent extends ComponentDecorator<Container> implements IVideoComponent {

	private Recording recording;
	protected Frame fullScreenFrame;
	protected MyInternalFrame internalFrame;
	private Timer timer;
	boolean fullscreen = false;

	public VideoComponent() {
		super();
	}

	public VideoComponent(Container component) {
		super(component);
	}

	public VideoComponent(Container component, String name) {
		super(component, name);
	}

	public VideoComponent(PropertyChangeListener listener) {
		addPropertyChangeListener(listener);
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
		return fullScreenFrame;
	}

	public JInternalFrame getInternalFrame() {
		return internalFrame.getComponent();
	}

	protected void setComponentTitle(String title) {
		if (isFullscreen()) {
			getFullscreenFrame().setTitle(title);
		} else {
			getInternalFrame().setTitle(title);
		}
	}

	@Override
	public Container getComponent() {
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

	protected void setFullscreen(boolean fullscreen) {
		this.firePropertyChange(FULLSCREEN, this.fullscreen, this.fullscreen = fullscreen);
	}

	public void toFront() {
		if (isFullscreen()) {
			getFullscreenFrame().toFront();
		} else {
			internalFrame.getComponent().toFront();
		}
	}

}