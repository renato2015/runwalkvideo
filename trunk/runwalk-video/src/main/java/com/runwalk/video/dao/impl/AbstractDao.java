package com.runwalk.video.dao.impl;

import com.runwalk.video.dao.Dao;

public abstract class AbstractDao<E> implements Dao<E> {

	private final Class<E> typeParameter;

	public AbstractDao(Class<E> typeParameter) {
		this.typeParameter = typeParameter;
	}

	public Class<E> getTypeParameter() {
		return typeParameter;
	}

}
