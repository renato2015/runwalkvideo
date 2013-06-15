package com.runwalk.video.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeDaoService implements DaoService {
	
	private final List<DaoService> daoServices;

	public CompositeDaoService(DaoService... daoServices) {
		this.daoServices = new ArrayList<DaoService>(Arrays.asList(daoServices));
	}
	
	/**
	 * This method will iterate over all {@link DaoService}s and return the first {@link Dao} found.
	 * 
	 * @return The dao found for the given class
	 */
	public <E, D extends Dao<E>> D getDao(Class<E> type) {
		D result = null;
		for(int i = 0; i < daoServices.size() && result == null; i++) {
			result = daoServices.get(i).getDao(type);
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void shutdown() {
		for(DaoService daoService : daoServices) {
			daoService.shutdown();
		}
	}

}
