package com.runwalk.video.gui.panels;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.gui.AppComponent;

@SuppressWarnings("serial")
public class AppPanel extends JPanel implements AppComponent { 
	
	public Action getAction(String name) {
		return getApplicationActionMap().get(name);
	}

	public RunwalkVideoApp getApplication() {
		return RunwalkVideoApp.getApplication();
	}

	public ApplicationContext getContext() {
		return getApplication().getContext();
	}

	public Logger getLogger() {
		return Logger.getLogger(getClass());
	}

	public ResourceMap getResourceMap() {
		return getContext().getResourceMap(getClass(), AppPanel.class);
	}
	
	public ActionMap getApplicationActionMap() {
		return getContext().getActionMap(AppPanel.class, this);
	}
}
