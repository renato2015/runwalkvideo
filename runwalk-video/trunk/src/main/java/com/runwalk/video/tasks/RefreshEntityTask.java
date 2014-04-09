package com.runwalk.video.tasks;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.SerializableEntity;

public class RefreshEntityTask<T extends SerializableEntity<? super T>> extends AbstractTask<T, Void> {

	private final T item;
	private final DaoService daoService;
	private final Class<T> itemClass;

	public RefreshEntityTask(DaoService daoService, Class<T> itemClass, T item) {
		super("refreshEntity");
		this.daoService = daoService;
		this.item = item;
		this.itemClass = itemClass;
	}

	protected T doInBackground() throws Exception {
		message("startMessage");
		Dao<T> itemDao = getDaoService().getDao(getItemClass());
		// get the last added entities
		T selectedItem = itemDao.getById(getItem().getId());
		// add selected item to the end of the list
		message("endMessage", selectedItem);
		return selectedItem;
	}

	private DaoService getDaoService() {
		return daoService;
	}
	
	private T getItem() {
		return item;
	}

	private Class<T> getItemClass() {
		return itemClass;
	}

}
