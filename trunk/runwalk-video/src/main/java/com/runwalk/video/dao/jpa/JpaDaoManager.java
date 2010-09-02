package com.runwalk.video.dao.jpa;

import javax.persistence.EntityManagerFactory;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoManager;

public class JpaDaoManager extends DaoManager {

	private final EntityManagerFactory entityManagerFactory;

	public JpaDaoManager(EntityManagerFactory entityManagerFactory) {
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
