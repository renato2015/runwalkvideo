package com.runwalk.video.core;

import java.awt.event.ActionEvent;


public interface FullScreenSupport extends SelfContained {

	public abstract void setFullScreen(boolean fullScreen);

	public abstract boolean isFullScreen();

	public abstract boolean isToggleFullScreenEnabled();

	public abstract void toggleFullScreen(ActionEvent event);

}