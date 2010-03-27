package com.runwalk.video.gui.media;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsDevice;

import javax.swing.ActionMap;

public interface IVideoComponent {

	public static final String FULLSCREEN = "fullscreen";

	public String getTitle();

	public Frame getFullscreenFrame();

	public Component getComponent();
	
	/**
	 * TODO deze method ook in de compresstask gebruiken!
	 */
	public void dispose();

	public boolean isActive();

	public void setFullScreen(GraphicsDevice graphicsDevice, boolean b);
	
	public ActionMap getActionMap();

}