package com.runwalk.video.gui.media;

import java.awt.Component;

import java.awt.Frame;
import java.awt.GraphicsDevice;

import javax.swing.ActionMap;

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
	 * TODO deze method ook in de compresstask gebruiken!
	 */
	public void dispose();

	public boolean isActive();

	public void setFullScreen(GraphicsDevice graphicsDevice, boolean b);
	
	public ActionMap getActionMap();

}