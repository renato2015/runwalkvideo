package com.runwalk.video.gui.actions;

import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;

@SuppressWarnings("serial")
abstract public class ResourceInjector extends javax.swing.AbstractAction {
	
	public static javax.swing.Action injectResources(javax.swing.Action action) {
		return injectResources(action, action.getValue(NAME).toString());
	}
	
	public static javax.swing.Action injectResources(javax.swing.Action action, String name) {
		ResourceMap map = RunwalkVideoApp.getApplication().getContext().getResourceMap(ApplicationActions.class);
		action.putValue(NAME, map.getString(name + ".Action.text"));
		action.putValue(SHORT_DESCRIPTION, map.getString(name + ".Action.description"));
		action.putValue(MNEMONIC_KEY, map.getKeyCode(name + ".Action.mnemonic"));
		action.putValue(ACCELERATOR_KEY, map.getKeyStroke(name + ".Action.accelerator"));
//		action.putValue(SMALL_ICON, name + ".Action.icon");
		return action;
	}
	
	public static String injectResources(String resourceName, Class<?> theClass) {
		ResourceMap map = RunwalkVideoApp.getApplication().getContext().getResourceMap(theClass);
		return map.getString(resourceName);
	}

}
