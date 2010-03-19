package com.runwalk.video.gui.media;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsDevice;

public interface IVideoComponent {

	public static final String FULLSCREEN = "fullscreen";

	public String getName();

	public Frame getFullscreenFrame();

	public Component getComponent();
	
	/**
	 * TODO deze method ook in de compresstask gebruiken!
	 */
	public void dispose();

	public boolean isActive();

	public void toggleFullScreen(GraphicsDevice graphicsDevice, boolean b);

}