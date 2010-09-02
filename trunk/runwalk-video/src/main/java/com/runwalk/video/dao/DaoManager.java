package com.runwalk.video.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.runwalk.video.dao.jpa.JpaClientDao;

public abstract class DaoManager {

	private final Map<Class<?>,Dao<?>> daos = new HashMap<Class<?>, Dao<?>>();

	public void setDaos(Set<? extends Dao<?>> daos) {
		synchronized (getDaos()) {
			getDaos().clear();
			for (Dao<?> dao : getDaos().values()) {
				getDaos().put(dao.getTypeParameter(), dao);
			}
		}
	}
	
	protected Map<Class<?>, Dao<?>> getDaos() {
		return daos;
	}

	public abstract <E, D extends Dao<E>> D getDao(Class<E> type);

	public void addDao(JpaClientDao clientDao) {
		getDaos().put(clientDao.getTypeParameter(), clientDao);
	}
	
	/**
	 * 
	 */
	public abstract void shutdown();

}
