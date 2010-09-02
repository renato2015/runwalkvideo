package com.runwalk.video.dao;

import javax.persistence.EntityManager;


public abstract class AbstractDAO<E> implements DAO<E> {

	private final Class<E> typeParameter;
	
	private EntityManager entityManager;

	public AbstractDAO(Class<E> typeParameter) {
		this.typeParameter = typeParameter;
	}

	public Class<E> getTypeParameter() {
		return typeParameter;
	}

	/** {@inheritDoc} */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	protected EntityManager getEntityManager() {
		return entityManager;
	}
	
}
