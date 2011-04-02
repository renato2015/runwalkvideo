package com.runwalk.video.media;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.image.BufferedImage;

/**
 * This class can be seen as the "implementor" of the used bridge pattern for providing the abstract {@link VideoPlayer} with a concrete
 * (native) implementation. 
 * 
 * @author Jeroen Peelaerts
 *
 */
public interface IVideoComponent {

	String FULLSCREEN = "fullscreen";
	
	String VISIBLE = "visible";
	
	String FULL_SCREEN_ENABLED = "fullScreenEnabled";
	
	String getTitle();

	/**
	 * Dispose all the resources involved for showing this component to screen.
	 */
	void dispose();

	boolean isActive();
	
	Dimension getDimension();
	
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
	 * Get an AWT {@link Component} in which the video will be rendered for windowed mode.
	 * @return The component
	 */
	Component getComponent();
	
	Frame getFullscreenFrame();
	
	/**
	 * Set a {@link BufferedImage} and draw it on top of the rendering component.
	 * Setting the image to null should clear all the drawings.
	 * 
	 * @param image The image to draw
	 * @param alphaColor The color to use for transparancy
	 */
	void setOverlayImage(BufferedImage image, Color alphaColor);
	
	BufferedImage getImage();
	
	boolean isFullScreenEnabled();
	
	boolean isFullScreen();
	
	void setFullScreen(boolean fullScreen, Integer monitorId);
	
	boolean isVisible();
	
	void setVisible(boolean visible);

	void setTitle(String title);

	void toFront();
	
}