package com.runwalk.video.entities;

import java.io.Serializable;
import org.jdesktop.application.AbstractBean;

@SuppressWarnings("serial")
public abstract class SerializableEntity<T> extends AbstractBean implements Comparable<T>, Serializable {

	public abstract Long getId();
	
	
	
}
