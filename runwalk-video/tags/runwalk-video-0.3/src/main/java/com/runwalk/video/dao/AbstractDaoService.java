package com.runwalk.video.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractDaoService implements DaoService {

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

	public void addDao(Dao<?> dao) {
		getDaos().put(dao.getTypeParameter(), dao);
	}
	
	public abstract void shutdown();

}
