package com.runwalk.video.aspects;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.ui.AppComponent;
import com.runwalk.video.ui.IAppComponent;

/**
 * This aspect will add common BSAF functionalities to classes implementing the {@link AppComponent} marker interface.
 * 
 * @author Jeroen Peelaerts
 */
public aspect AppComponentAspect {
	
	declare parents: @AppComponent * implements IAppComponent;

	public javax.swing.Action IAppComponent.getAction(String name) {
		return getApplicationActionMap().get(name);
	}

	public RunwalkVideoApp IAppComponent.getApplication() {
		return RunwalkVideoApp.getApplication();
	}
	
	public ApplicationContext IAppComponent.getContext() {
		return getApplication().getContext();
	}

	public Logger IAppComponent.getLogger() {
		return Logger.getLogger(getClass());
	}

	public ResourceMap IAppComponent.getResourceMap(Class<?> theClass) {
		return getContext().getResourceMap(theClass);
	}
	
	public ResourceMap IAppComponent.getResourceMap() {
		return getContext().getResourceMap(getClass(), IAppComponent.class);
	}
	
	public ApplicationActionMap IAppComponent.getApplicationActionMap(Class<?> stopClass, Object instance) {
		Class<?> theClass = instance.getClass();
		while(stopClass.isInterface() && theClass.getSuperclass() != null) {
			theClass = theClass.getSuperclass();
			List<Class<?>> interfaces = Arrays.asList(theClass.getInterfaces());
			if (interfaces.contains(stopClass)) {
				// theClass won't be an interface anymore
				stopClass = theClass;
			}
		}
		stopClass = stopClass.isInterface() ? theClass : stopClass;
		return getContext().getActionMap(stopClass, instance);
	}

	public ApplicationActionMap IAppComponent.getApplicationActionMap() {
		Class<?> stopClass = getStopClass(getClass());
		return getContext().getActionMap(stopClass, this);
	}
	
	private Class<?> IAppComponent.getStopClass(Class<?> theClass) {
		while(theClass.getSuperclass() != null) {
			theClass = theClass.getSuperclass();
		}
		return theClass;
	}
	
}
