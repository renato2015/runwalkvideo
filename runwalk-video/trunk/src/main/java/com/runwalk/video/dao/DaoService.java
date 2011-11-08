package com.runwalk.video.dao;

import java.util.Set;

public interface DaoService {

	public abstract void setDaos(Set<? extends Dao<?>> daos);

	public abstract <E, D extends Dao<E>> D getDao(Class<E> type);

	public abstract void addDao(Dao<?> dao);

	/**
	 * Close current persistence context and clean up all resources.
	 */
	public abstract void shutdown();

}