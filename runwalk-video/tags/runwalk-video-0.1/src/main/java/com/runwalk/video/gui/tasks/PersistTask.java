package com.runwalk.video.gui.tasks;

import com.runwalk.video.dao.DaoService;

public class PersistTask<T> extends AbstractTask<T, Void> {

	private final T item;
	
	private final DaoService daoService;

	private final Class<T> itemClass;
	
	public PersistTask(DaoService daoService, Class<T> itemClass, T item) {
		super("persist");
		this.item = item;
		this.daoService = daoService;
		this.itemClass = itemClass;
	}

	protected T doInBackground() {
		message("startMessage");
		getDaoService().getDao(getItemClass()).persist(getItem());
		message("endMessage");
		return getItem();
	}
	
	private DaoService getDaoService() {
		return this.daoService;
	}
	
	private Class<T> getItemClass() {
		return this.itemClass;
	}

	private T getItem() {
		return item;
	}
	
}
