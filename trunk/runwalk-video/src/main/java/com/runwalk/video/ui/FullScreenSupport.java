package com.runwalk.video.ui;


public interface FullScreenSupport extends SelfContained {

	public abstract void setFullScreen(boolean fullScreen);

	public abstract boolean isFullScreen();

	public abstract boolean isToggleFullScreenEnabled();

	public abstract void toggleFullScreen();

}