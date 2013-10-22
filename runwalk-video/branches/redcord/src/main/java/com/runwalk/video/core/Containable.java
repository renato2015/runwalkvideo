package com.runwalk.video.core;

import java.awt.Component;
import java.awt.Container;

/**
 * {@link Containable}'s are components designed to be contained by a {@link Container}.
 * 
 * @author Jeroen Peelaerts
 *
 */
@AppComponent
public interface Containable extends WindowConstants {
	
	/**
	 * Get an AWT {@link Component} in which the video will be rendered for windowed mode.
	 * @return The component
	 */
	Component getComponent();
	
	boolean isResizable();
	
	String getTitle();

}
