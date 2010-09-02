package com.runwalk.video.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

public class DAOManager {

	private final Map<Class<?>,DAO<?>> daos = new HashMap<Class<?>, DAO<?>>();
	
	private EntityManagerFactory entityManagerFactory;
	
	public DAOManager(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}
	
	public void setDaos(Set<? extends DAO<?>> daos) {
		synchronized (this.daos) {
			this.daos.clear();
			for (DAO<?> dao : daos) {
				this.daos.put(dao.getTypeParameter(), dao);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <E,D extends DAO<E>> D getDAO(Class<E> type) {
		D result = (D) daos.get(type);
		// inject entity manager as it is application managed
		result.setEntityManager(getEntityManagerFactory().createEntityManager());
		return result;
	}

	private EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}
	
}
