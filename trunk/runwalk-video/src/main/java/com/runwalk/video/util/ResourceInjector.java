package com.runwalk.video.util;

import javax.swing.Action;

import org.jdesktop.application.ResourceMap;

import com.runwalk.video.gui.AppComponent;
import com.runwalk.video.gui.actions.ApplicationActions;

public class ResourceInjector implements AppComponent {
	
	private final static ResourceInjector INSTANCE = new ResourceInjector();
	
	private ResourceInjector() { }

	public static ResourceInjector getInstance() {
		return INSTANCE;
	}
	
	public javax.swing.Action injectResources(Action action) {
		String actionName = action.getValue(Action.NAME).toString();
		actionName = Character.toLowerCase(actionName.charAt(0)) + actionName.substring(1);
		return injectResources(action, actionName, ApplicationActions.class);
	}
	
	public javax.swing.Action injectResources(javax.swing.Action action, String name, Class<?> theClass) {
		ResourceMap resourceMap = getContext().getResourceMap(theClass);
		action.putValue(Action.NAME, resourceMap.getString(name + ".Action.text"));
		action.putValue(Action.SHORT_DESCRIPTION, resourceMap.getString(name + ".Action.description"));
		action.putValue(Action.MNEMONIC_KEY, resourceMap.getKeyCode(name + ".Action.mnemonic"));
		action.putValue(Action.ACCELERATOR_KEY, resourceMap.getKeyStroke(name + ".Action.accelerator"));
		action.putValue(Action.SMALL_ICON, resourceMap.getIcon(name + ".Action.icon"));
		return action;
	}
	
	public String injectResources(String resourceName, Class<?> theClass) {
		ResourceMap map = getContext().getResourceMap(theClass);
		return map.getString(resourceName);
	}

}
