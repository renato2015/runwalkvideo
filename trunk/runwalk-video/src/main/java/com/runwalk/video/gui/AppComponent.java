package com.runwalk.video.gui;

import javax.swing.Action;
import javax.swing.ActionMap;

import org.apache.log4j.Logger;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;

public interface AppComponent {
	
	public RunwalkVideoApp getApplication();

	public ResourceMap getResourceMap();
	
	public ApplicationContext getContext();
	
	public Action getAction(String name);
	
	public ActionMap getApplicationActionMap();
	
	public Logger getLogger();

}
