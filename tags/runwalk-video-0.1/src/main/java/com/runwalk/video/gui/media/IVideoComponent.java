package com.runwalk.video.gui.media;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsDevice;

/**
 * This class can be seen as the "implementor" of the used bridge pattern for providing the abstract {@link VideoPlayer} with a concrete
 * (native) implementation. 
 * 
 * @author Jeroen Peelaerts
 *
 */
public interface IVideoComponent {

	public static final String FULLSCREEN = "fullscreen";

	public String getTitle();

	public Frame getFullscreenFrame();

	/**
	 * Get an AWT {@link Component} in which the video will be rendered in windowed mode.
	 * @return The component
	 */
	public Component getComponent();
	
	/**
	 * Dispose all the resources involved for showing this component to screen.
	 */
	public void dispose();

	public boolean isActive();

	public void setFullScreen(GraphicsDevice graphicsDevice, boolean b);
	
}