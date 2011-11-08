package com.runwalk.video.util;

import javax.swing.ActionMap;

import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;

import com.runwalk.video.ui.AppComponent;

@AppComponent
public class TaskExecutor<T, V> extends TaskListener.Adapter<T, V> {

	private final String actionName;

	private ActionMap actionMap;
	
	public TaskExecutor(ActionMap actionMap, String actionName) {
		this.actionMap = actionMap;
		this.actionName = actionName;
	}
	
	public TaskExecutor(String actionName) {
		this.actionName = actionName;
	}
	
	@Override
	public void finished(TaskEvent<Void> event) {
		getApplication().executeAction(getActionMap(), getActionName());
	}

	public ActionMap getActionMap() {
		return actionMap == null ? getApplicationActionMap() : actionMap;
	}

	public String getActionName() {
		return actionName;
	}

}
