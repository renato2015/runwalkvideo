package com.runwalk.video.gui.tasks;

import java.util.concurrent.TimeUnit;

import com.runwalk.video.dao.DaoService;

public class DeleteTask<T> extends AbstractTask<T, Void> {

	private final T item;
	
	private final DaoService daoService;

	private final Class<T> itemClass;
	
	public DeleteTask(DaoService daoService, Class<T> itemClass, T item) {
		super("delete");
		this.item = item;
		this.daoService = daoService;
		this.itemClass = itemClass;
	}

	protected T doInBackground() {
		message("startMessage");
		getDaoService().getDao(getItemClass()).delete(getItem());
		message("endMessage", getExecutionDuration(TimeUnit.MILLISECONDS));
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
