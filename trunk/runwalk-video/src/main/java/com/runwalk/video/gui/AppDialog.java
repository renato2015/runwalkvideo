package com.runwalk.video.gui;

import java.awt.Frame;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JDialog;

import org.apache.log4j.Logger;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;

@SuppressWarnings("serial")
public class AppDialog extends JDialog implements AppComponent {

	public AppDialog() {
		super();
	}

	public AppDialog(Frame owner) {
		super(owner);
	}
	
	public AppDialog(Frame parent, boolean b) {
		super(parent, b);
	}

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
		return getContext().getResourceMap(getClass(), AppDialog.class);
	}
	
	public ActionMap getApplicationActionMap() {
		return getContext().getActionMap(AppDialog.class, this);
	}


}
