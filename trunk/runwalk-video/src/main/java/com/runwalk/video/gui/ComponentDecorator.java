package com.runwalk.video.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.util.AppSettings;

public abstract class ComponentDecorator<T extends Container> extends AbstractBean {
	/**
	 * The component that will be decorated. This will always be a subclass of {@link JComponent}.
	 */
	private T component;
	
	private ApplicationContext context;
	
	protected ComponentDecorator() {
		this.context = Application.getInstance().getContext();
	}
	
	protected ComponentDecorator(T component) {
		this();
		this.component = component;
		getComponent().setFont(AppSettings.MAIN_FONT);
	}
	
	protected ComponentDecorator(T component, String name) {
		this(component);
		setName(name);
		//ready to inject resources..
	}

	public void add(Component comp, Object obj) {
//		comp.setFont(ApplicationSettings.MAIN_FONT);
		getComponent().add(comp, obj);
	}
	
	public void add(Component comp) {
		add(comp, null);
	}
	
	public void setName(String name) {
		getComponent().setName(name);
	}
	
	public String getName() {
		return getComponent().getName();
	}
	
	public void setPreferredSize(Dimension dimension) {
		getComponent().setPreferredSize(dimension);
	}
	
	public void setEnabled(boolean enabled) {
		getComponent().setEnabled(enabled);
	}
	
	public boolean isVisible() {
		return getComponent().isVisible();
	}
	
	public void setVisible(boolean visible) {
		getComponent().setVisible(visible);
	}
	
	public boolean isAncestorOf(Component component) {
		return getComponent().isAncestorOf(component);
	}
	
	public void remove(Component asComponent) {
		getComponent().remove(asComponent);
	}
	
	protected RunwalkVideoApp getApplication() {
		return RunwalkVideoApp.getApplication();
	}
	
	public void setLayout(LayoutManager mgr) {
		getComponent().setLayout(mgr);
	}
	
	public ResourceMap getResourceMap() {
		return getContext().getResourceMap(getClass(), ComponentDecorator.class);
	}
	
	protected ApplicationContext getContext() {
		return context;
	}
	
	protected void setComponent(T component) {
		this.component = component;
	}
	
	public T getComponent() {
		return component;
	}
	
	public Action getAction(String name) {
		return getActionMap().get(name);
	}
	
	protected ActionMap getActionMap() {
		return getContext().getActionMap(ComponentDecorator.class, this);
	}
	
	protected Logger getLogger() {
		return Logger.getLogger(getClass());
	}

}
