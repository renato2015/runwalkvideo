package com.runwalk.video.media;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * This class can be seen as the "implementor" of the used bridge pattern for providing the abstract {@link VideoPlayer} with a concrete
 * (native) implementation. 
 * 
 * @author Jeroen Peelaerts
 *
 */
public interface IVideoComponent {
	
	/**
	 * Start running and bring the component to a state in which the {@link Component} returned by {@link #getComponent()} 
	 * or the {@link java.awt.Window} returned by {@link #getFullscreenFrame()} are initialized and ready to show video.
	 */
	void startRunning();

	 /**
	 * Stop running and bring the component to a state in which video format settings can be applied. 
	 * In most cases the component will have to stop previewing video in order to reconfigure properly.
	 */
	void stopRunning();
	
	/**
	 * Dispose all the resources involved for showing this component to screen.
	 */
	void dispose();

	boolean isActive();
	
	Dimension getDimension();
	
	/**
	 * Set a {@link BufferedImage} and draw it on top of the rendering component.
	 * Setting the image to null should clear all the drawings.
	 * 
	 * @param image The image to draw
	 * @param alphaColor The color to use for transparancy
	 */
	void setOverlayImage(BufferedImage image, Color alphaColor);
	
	BufferedImage getImage();
	
	String getTitle();
	
}