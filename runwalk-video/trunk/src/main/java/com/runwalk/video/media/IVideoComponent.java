package com.runwalk.video.media;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;

import com.google.gdata.data.extensions.Image;

/**
 * This class can be seen as the "implementor" of the used bridge pattern for providing the abstract {@link VideoPlayer} with a concrete
 * (native) implementation. 
 * 
 * @author Jeroen Peelaerts
 *
 */
public interface IVideoComponent extends HierarchyListener {
	
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
	 * Get the monitor id for this component.
	 * Should return <code>null</code> if running on the main screen.
	 * 
	 * @return The monitor id.
	 */
	Integer getMonitorId();
	
	/**
	 * Set a {@link BufferedImage} and draw it on top of the rendering component.
	 * Setting the image to null should clear all the drawings.
	 * 
	 * @param image The image to draw
	 * @param alphaColor The color to use for transparancy
	 */
	void setOverlayImage(BufferedImage image, Color alphaColor);
	
	/**
	 * Get the currently showing {@link Image} of this video implementation.
	 * @return The image currently shown
	 */
	BufferedImage getImage();
	
	/**
	 * Get the component's title.
	 * @return The title
	 */
	String getTitle();
	
	/**
	 * Returns <code>true</code> if the component is currently visible on screen.
	 * 
	 * @return <code>true</code> if component is visible on screen
	 */
	boolean isVisible();

	/**
	 * Returns <code>true</code> if the containing window is created in native code.
	 * 
	 * @return <code>true</code> if window creation does not reside in Swing
	 */
	boolean isNativeWindowing();
	
}