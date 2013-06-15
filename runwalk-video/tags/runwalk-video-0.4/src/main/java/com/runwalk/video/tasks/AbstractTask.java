package com.runwalk.video.tasks;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.util.Collections;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;

public abstract class AbstractTask<T, V> extends Task<T, V> {
	private static final String ERROR_MESSAGE = "errorMessage";

	private final Logger logger;

	@SuppressWarnings("deprecation")
	public AbstractTask(String name) {
		super(Application.getInstance(), name);
		logger = Logger.getLogger(getClass());
	}

	protected void errorMessage(String formatResourceKey, Object... args) { 
		ResourceMap resourceMap = getResourceMap();
		if (resourceMap != null) {
			firePropertyChange(ERROR_MESSAGE, getMessage(), getResourceString(formatResourceKey, args));
		}
	}

	protected String getResourceString(String string, Object... args) {
		ResourceMap resourceMap = getResourceMap();
		String result = null;
		if (resourceMap != null) {
			result = getResourceMap().getString(resourceName(string), args);
		}
		return result;
	}
	
	protected void failed(Throwable throwable, Object... variables) {
		Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		if (activeWindow == null || !activeWindow.getName().equals("mainFrame")) {
			activeWindow = null;
		}
		getLogger().error(throwable.getMessage(), throwable);
		JOptionPane.showMessageDialog(
				activeWindow, 
				getErrorMessage(throwable, variables),
				getResourceString(ERROR_MESSAGE), 
				JOptionPane.ERROR_MESSAGE);
	}

	@Override
	protected void failed(Throwable throwable) {
		failed(throwable, Collections.emptyList().toArray());
	}

	/**
	 * Fetch a human readable error description from the {@link Task}'s property file.
	 * 
	 * @param throwable The throwable to find an error description for
	 * @param args The arguments to be injected in the error description
	 * @return The error description
	 */
	private Object getErrorMessage(Throwable throwable, Object... args) {
		String simpleClassName = throwable.getClass().getSimpleName();
		String resourceSuffix = Character.toLowerCase(simpleClassName.charAt(0)) + simpleClassName.substring(1);
		Object errorMessage = getResourceString(resourceSuffix, args);
		// go through nested exceptions and look for a specific message..
		if (errorMessage == null && throwable.getCause() != null) {
			errorMessage = getErrorMessage(throwable.getCause(), args);
		}
		return errorMessage == null || errorMessage == throwable.getCause() ? throwable : errorMessage;
	}

	public Logger getLogger() {
		return logger;
	}

	public static class CursorInputBlocker extends Task.InputBlocker {

		private Component rootPane;

		public CursorInputBlocker(@SuppressWarnings("rawtypes") Task arg0, Component rootPane) {
			super(arg0, BlockingScope.COMPONENT, rootPane);
			this.rootPane = rootPane;
		}

		protected void block() {
			rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		protected void unblock() {
			rootPane.setCursor(Cursor.getDefaultCursor());
			rootPane = null;
		}

	}

}
