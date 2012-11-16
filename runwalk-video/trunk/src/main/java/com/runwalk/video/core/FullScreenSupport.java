package com.runwalk.video.core;

import java.awt.event.ActionEvent;


public interface FullScreenSupport extends SelfContained {
	
	void enterFullScreen();

	void leaveFullScreen();
	
	boolean isFullScreen();

	boolean isToggleFullScreenEnabled();

	void toggleFullScreen(ActionEvent event);

}