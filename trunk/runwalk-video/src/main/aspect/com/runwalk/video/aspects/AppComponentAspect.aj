package com.runwalk.video.aspects;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.ActionMap;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;
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
	
	/**
	 * This method will look for an {@link javax.swing.Action} specified with the given key in the given {@link ActionMap} 
	 * and invoke its {@link Action#actionPerformed(ActionEvent)} method. If the specified {@link javax.swing.Action} has
	 * a {@link javax.swing.Action#SELECTED_KEY} set, then this method will invert its value.
	 * 
	 * @param action The {@link Action} to be executed
	 * @param component The component used as the {@link Action}'s source
	 */
	public void IAppComponent.invokeAction(String actionName, Object source) {
		invokeAction(actionName, getApplicationActionMap(), source);
	}
	
	public void IAppComponent.invokeAction(String actionName, ActionMap actionMap, Object source) {
		javax.swing.Action action = actionMap.get(actionName);
		if (action != null) {
			ActionEvent actionEvent = new ActionEvent(source, ActionEvent.ACTION_PERFORMED, action.toString());
			/*Object selected = action.getValue(javax.swing.Action.SELECTED_KEY);
			if (selected != null) {
				Boolean newValue = !((Boolean) selected).booleanValue();
				if (action instanceof ApplicationAction) {
					((ApplicationAction) action).setSelected(newValue);
				} else {
					action.putValue(javax.swing.Action.SELECTED_KEY, newValue);
				}
			}*/
			action.actionPerformed(actionEvent);
		}
	}
	
	private Class<?> IAppComponent.getStopClass(Class<?> theClass) {
		while(theClass.getSuperclass() != null) {
			theClass = theClass.getSuperclass();
		}
		return theClass;
	}
	
}
