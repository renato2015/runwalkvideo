package com.runwalk.video.dao;

import org.apache.log4j.Logger;


public abstract class AbstractDao<E> implements Dao<E> {

	private final Class<E> typeParameter;
	
	private final Logger logger = Logger.getLogger(getClass());

	public AbstractDao(Class<E> typeParameter) {
		this.typeParameter = typeParameter;
	}

	public Class<E> getTypeParameter() {
		return typeParameter;
	}
	
	protected Logger getLogger() {
		return logger;
	}
	
}
