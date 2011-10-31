package com.runwalk.video.util;

import java.awt.Component;

import javax.swing.ActionMap;

import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;

import com.runwalk.video.core.AppComponent;

@AppComponent
public class TaskExecutor<T, V> extends TaskListener.Adapter<T, V> {

	private final String actionName;

	private final ActionMap actionMap;
	
	private final Component source;
	
	public TaskExecutor(ActionMap actionMap, String actionName, Component source) {
		this.actionMap = actionMap;
		this.actionName = actionName;
		this.source = source;
	}
	
	@Override
	public void finished(TaskEvent<Void> event) {
		invokeAction(getActionName(), getActionMap(), getSource());
	}

	public ActionMap getActionMap() {
		return actionMap == null ? getApplicationActionMap() : actionMap;
	}

	public String getActionName() {
		return actionName;
	}

	public Component getSource() {
		return source;
	}
	
}
