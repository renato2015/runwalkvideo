package com.runwalk.video.dao;


public interface DaoService {

	/**
	 * Return the {@link Dao} for the given class.
	 * @param type The class to find a dao for
	 * @return A dao or null if none found
	 */
	public abstract <E, D extends Dao<E>> D getDao(Class<E> type);

	/**
	 * Close current persistence context and clean up all resources.
	 */
	public abstract void shutdown();

}