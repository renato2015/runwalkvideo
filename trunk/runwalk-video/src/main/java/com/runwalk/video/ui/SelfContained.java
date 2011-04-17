package com.runwalk.video.ui;

import java.awt.Window;
import java.beans.PropertyChangeListener;

import org.jdesktop.application.Action;

import com.tomtessier.scrollabledesktop.BaseInternalFrame;

/**
 * This class will wrap a {@link BaseInternalFrame} or a {@link Window} for use with the application windowing framework.
 * 
 * @author Jeroen Peelaerts
 *
 */
@AppComponent(actionMapStopClass = SelfContained.class)
public interface SelfContained extends WindowConstants {

	/**
	 * Make the implementor eligible for garbage collection.
	 */
	public void dispose();
	
	/**
	 * The title for this component. This should be a unique across the application
	 * @return The title
	 */
	public String getTitle();

	public boolean isVisible();
	
	public void setVisible(boolean visible);
	
	@Action(selectedProperty = VISIBLE)
	public void toggleVisibility();
	
	public void toFront();
	
	public void setFullScreen(boolean fullScreen, Integer monitorId);
	
	public boolean isFullScreen();
	
	public boolean isToggleFullScreenEnabled();
	
	@Action(selectedProperty = FULL_SCREEN, enabledProperty = TOGGLE_FULL_SCREEN_ENABLED)
	public void toggleFullScreen();
	
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	public void removePropertyChangeListener(PropertyChangeListener listener);
	
}
