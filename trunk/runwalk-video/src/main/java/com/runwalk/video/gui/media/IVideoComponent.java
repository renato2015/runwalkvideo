package com.runwalk.video.gui.media;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.image.BufferedImage;

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
	 * Get an AWT {@link Component} in which the video will be rendered for windowed mode.
	 * @return The component
	 */
	public Component getComponent();
	
	/**
	 * Dispose all the resources involved for showing this component to screen.
	 */
	public void dispose();

	public boolean isActive();

	public void setFullScreen(GraphicsDevice graphicsDevice, boolean b);
	
	/**
	 * Set a {@link BufferedImage} and draw it on top of the rendering component.
	 * Setting the image to null should clear all the drawings.
	 * 
	 * @param image The image to draw
	 * @param alphaColor The color to use for transparancy
	 */
	public void setOverlayImage(BufferedImage image, Color alphaColor);
	
	public BufferedImage getImage();
	
}