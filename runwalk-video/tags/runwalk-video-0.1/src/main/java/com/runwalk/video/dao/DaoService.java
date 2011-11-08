package com.runwalk.video.dao;

import java.util.Set;

import com.runwalk.video.dao.impl.JpaClientDao;

public interface DaoService {

	public abstract void setDaos(Set<? extends Dao<?>> daos);

	public abstract <E, D extends Dao<E>> D getDao(Class<E> type);

	public abstract void addDao(JpaClientDao clientDao);

	/**
	 * Close current persistence context and clean up all resources.
	 */
	public abstract void shutdown();

}