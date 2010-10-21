package com.runwalk.video.aspects;

import javax.swing.ActionMap;

import org.apache.log4j.Logger;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.gui.AppComponent;

/**
 * This aspect will add common BSAF functionalities to classes implementing the {@link AppComponent} marker interface.
 * 
 * @author Jeroen Peelaerts
 */
public aspect AppComponentAspect {
	
	declare parents: com.runwalk.video.*AppComponent+ implements AppComponent;
	
	public javax.swing.Action AppComponent.getAction(String name) {
		return getApplicationActionMap().get(name);
	}

	public RunwalkVideoApp AppComponent.getApplication() {
		return RunwalkVideoApp.getApplication();
	}

	public ApplicationContext AppComponent.getContext() {
		return getApplication().getContext();
	}

	public Logger AppComponent.getLogger() {
		return Logger.getLogger(getClass());
	}

	public ResourceMap AppComponent.getResourceMap() {
		return getContext().getResourceMap(getClass(), AppComponent.class);
	}

	public ActionMap AppComponent.getApplicationActionMap() {
		return getContext().getActionMap(getClass(), this);
	}
	
	public ActionMap AppComponent.getApplicationActionMap(Class<?> stopClass) {
		return getContext().getActionMap(stopClass, this);
	}
	
}
