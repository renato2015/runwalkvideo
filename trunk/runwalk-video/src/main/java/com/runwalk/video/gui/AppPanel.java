package com.runwalk.video.gui;

import javax.swing.Action;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;

@SuppressWarnings("serial")
public class AppPanel extends JPanel implements AppComponent { 

	public Action getAction(String name) {
		return getApplicationActionMap().get(name);
	}

	public RunwalkVideoApp getApplication() {
		return RunwalkVideoApp.getApplication();
	}

	public JPanel getComponent() {
		return this;
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
	
	public ApplicationActionMap getApplicationActionMap() {
		return getContext().getActionMap(AppComponent.class, this);
	}
}