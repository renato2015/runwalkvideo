package com.runwalk.video.dao;

public abstract class AbstractDao<E> implements Dao<E> {

	private final Class<E> typeParameter;

	public AbstractDao(Class<E> typeParameter) {
		this.typeParameter = typeParameter;
	}

	public Class<E> getTypeParameter() {
		return typeParameter;
	}

}
