package com.runwalk.video.gui.tasks;

import org.apache.log4j.Logger;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;

import com.runwalk.video.RunwalkVideoApp;

public abstract class AbstractTask<T, V> extends Task<T, V> {
	protected final Logger logger;
	
	@SuppressWarnings("deprecation")
	public AbstractTask(String name) {
		super(RunwalkVideoApp.getApplication(), name);
		logger = Logger.getLogger(getClass());
	}
    
    protected void errorMessage(String formatResourceKey, Object... args) { 
    	ResourceMap resourceMap = getResourceMap();
    	if (resourceMap != null) {
    		firePropertyChange("errorMessage", getMessage(), getResourceString(formatResourceKey, args));
    	}
    }

	@Override
	protected void finished() {
		RunwalkVideoApp.getApplication().getStatusPanel().getProgressBar().updateUI();
	}
	
	protected String getResourceString(String string, Object... args) {
		ResourceMap resourceMap = getResourceMap();
		String result = null;
    	if (resourceMap != null) {
    		result = getResourceMap().getString(resourceName(string), args);
    	}
    	return result;
	}
	
}
