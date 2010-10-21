package com.runwalk.video.gui;

import java.awt.Container;
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
public interface AppWindowWrapper extends AppComponent {
	
	public static final String TOGGLE_VISIBILITY_ACTION = "toggleVisibility";
	public static final String VISIBLE = "visible";

	/**
	 * Make the implementor eligible for garbage collection.
	 */
	public void dispose();
	
	public Container getHolder();
	
	public String getTitle();
	
	public boolean isVisible();
	
	public void setVisible(boolean visible);
	
	@Action(selectedProperty = VISIBLE)
	public void toggleVisibility();
	
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	public void removePropertyChangeListener(PropertyChangeListener listener);
	
}
