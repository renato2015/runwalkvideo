package com.runwalk.video.aspects;

import java.util.Iterator;
import java.util.List;

import javax.swing.ActionMap;

import org.apache.log4j.Logger;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.google.common.collect.Lists;
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

	public ActionMap IAppComponent.getApplicationActionMap() {
		AppComponent annotation = getAnnotationRecursively(getClass());
		Class<?> stopClass = annotation == null ? getClass() : annotation.actionMapStopClass();
		return getContext().getActionMap(stopClass, this);
	}
	
	private AppComponent IAppComponent.getAnnotationRecursively(Class<?> theClass) {
		AppComponent annotation = theClass.getAnnotation(AppComponent.class);
		while(annotation == null && theClass != null) {
			List<Class<?>> list = Lists.asList(theClass, theClass.getInterfaces());
			Iterator<Class<?>> iterator = list.iterator();
			while(iterator.hasNext() && annotation == null) {
				Class<?> next = iterator.next();
				annotation = next.getAnnotation(AppComponent.class);
			}
			theClass = theClass.getSuperclass();
		}
		return annotation;
	}
	
}
