package com.runwalk.video.ui;

import java.awt.Component;

public interface Floatable {

	/**
	 * Get an AWT {@link Component} in which the video will be rendered for windowed mode.
	 * @return The component
	 */
	Component getComponent();
}
