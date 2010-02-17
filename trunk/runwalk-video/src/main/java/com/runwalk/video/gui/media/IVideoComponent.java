package com.runwalk.video.gui.media;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsDevice;

import javax.swing.JInternalFrame;

import com.runwalk.video.entities.Recording;

public interface IVideoComponent {

	public static final String FULLSCREEN = "fullscreen";

	public Recording getRecording();

	public void setRecording(Recording recording);

	public String getName();

	public Frame getFullscreenFrame();

	public JInternalFrame getInternalFrame();

	//TODO dit zou een actie moeten worden, het GraphicsDevice moet vooraf gekozen worden!
	//TODO eventueel nakijken hoe heet afsluiten of aansluiten van een scherm kan opgevangen worden
	public void toggleFullscreen(GraphicsDevice device);

	public boolean isFullscreen();

	public Container getComponent();

	public void toFront();

	/**
	 * TODO deze method ook in de compresstask gebruiken!
	 */
	public void dispose(boolean clearRecording);

	public boolean isActive();

}