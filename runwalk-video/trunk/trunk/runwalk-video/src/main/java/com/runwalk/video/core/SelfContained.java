package com.runwalk.video.core;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;


/**
 * This class will wrap a {@link BaseInternalFrame} or a {@link Window} for use with the application windowing framework.
 * 
 * @author Jeroen Peelaerts
 *
 */
@AppComponent
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
	
	public void toggleVisibility(ActionEvent event);
	
	public void toFront();
	
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	public void removePropertyChangeListener(PropertyChangeListener listener);

	public void setMonitorId(Integer monitorId);
	
	/** 
	 * The monitor id to use for fullscreen mode .
	 * @return The monitor id
	 */
	public Integer getMonitorId();
	
}
