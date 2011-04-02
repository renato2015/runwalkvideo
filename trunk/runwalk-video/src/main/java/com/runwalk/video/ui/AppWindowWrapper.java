package com.runwalk.video.ui;

import java.awt.Component;
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
@AppComponent(actionMapStopClass = AppWindowWrapper.class)
public interface AppWindowWrapper extends WindowConstants {

	/**
	 * Make the implementor eligible for garbage collection.
	 */
	public void dispose();
	
	public Component getHolder();
	
	public String getTitle();
	
	public boolean isVisible();
	
	public void setVisible(boolean visible);
	
	@Action(selectedProperty = VISIBLE)
	public void toggleVisibility();
	
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	public void removePropertyChangeListener(PropertyChangeListener listener);
	
}
