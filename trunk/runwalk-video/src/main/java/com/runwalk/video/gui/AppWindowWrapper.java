package com.runwalk.video.gui;

import java.awt.Container;
import java.awt.Window;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeListener;

import com.tomtessier.scrollabledesktop.BaseInternalFrame;

/**
 * This class will wrap a {@link BaseInternalFrame} or {@link Window} for use with the application's {@link VideoMenuBar}
 * 
 * @author Jeroen Peelaerts
 *
 */
public interface AppWindowWrapper extends AppComponent {
	
	/**
	 * Make the implementor eligible for garbage collection.
	 */
	public void dispose();
	
	public Container getHolder();
	
	public String getTitle();
	
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	public void removePropertyChangeListener(PropertyChangeListener listener);
	
    public ComponentListener[] getComponentListeners();
	
    public void addComponentListener(ComponentListener l);
    
    public void removeComponentListener(ComponentListener l);
    
}
