package com.runwalk.video.dao.impl;

import javax.persistence.EntityManagerFactory;

import com.runwalk.video.dao.AbstractDaoService;
import com.runwalk.video.dao.Dao;

public class JpaDaoService extends AbstractDaoService {

	private final EntityManagerFactory entityManagerFactory;

	public JpaDaoService(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}
	
	@SuppressWarnings("unchecked")
	public <E, D extends Dao<E>> D getDao(Class<E> type) {
		D result = (D) getDaos().get(type);
		if (result == null) {
			result = (D) new JpaDao<E>(type, entityManagerFactory);
			getDaos().put(type, result);
		}
		return result;
	}

	public void shutdown() {
		entityManagerFactory.close();
	}
	
}
