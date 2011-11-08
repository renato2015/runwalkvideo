package com.runwalk.video.core;

import java.awt.Component;


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
