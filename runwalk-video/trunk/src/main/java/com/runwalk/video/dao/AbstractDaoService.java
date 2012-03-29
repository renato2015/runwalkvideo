package com.runwalk.video.dao;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDaoService implements DaoService {

	private final Map<Class<?>,Dao<?>> daos = new HashMap<Class<?>, Dao<?>>();
	
	protected Map<Class<?>, Dao<?>> getDaos() {
		return daos;
	}
	
	@SuppressWarnings("unchecked")
	public <E, D extends Dao<E>> D getDao(Class<E> type) {
		return (D) getDaos().get(type);
	}

	public void addDao(Dao<?> dao) {
		getDaos().put(dao.getTypeParameter(), dao);
	}
	
	public void shutdown() {
		getDaos().clear();
	}

}
