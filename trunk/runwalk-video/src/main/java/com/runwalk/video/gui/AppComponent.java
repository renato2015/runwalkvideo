package com.runwalk.video.gui;

import javax.swing.Action;
import javax.swing.ActionMap;

import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

/**
 * This interface defines a basic set of methods providing access to some common Swing Application Framework (SAF) functionalities.
 * 
 * @author Jeroen Peelaerts
 *
 */
public interface AppComponent {
	
	public Application getApplication();

	public ResourceMap getResourceMap();
	
	public ApplicationContext getContext();
	
	public Action getAction(String name);
	
	public ActionMap getApplicationActionMap();
	
	public Logger getLogger();

}
