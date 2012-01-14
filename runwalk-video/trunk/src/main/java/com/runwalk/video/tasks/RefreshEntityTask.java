package com.runwalk.video.tasks;

import ca.odell.glazedlists.EventList;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.SerializableEntity;

public class RefreshEntityTask<T extends SerializableEntity<? super T>> extends AbstractTask<T, Void> {

	private final EventList<T> itemList;
	
	private final T item;

	private final DaoService daoService;

	private final Class<T> itemClass;

	public RefreshEntityTask(DaoService daoService, EventList<T> itemList, Class<T> itemClass, T item) {
		super("refreshEntity");
		this.daoService = daoService;
		this.itemList = itemList;
		this.item = item;
		this.itemClass = itemClass;
	}

	protected T doInBackground() throws Exception {
		message("startMessage");
		Dao<T> itemDao = getDaoService().getDao(getItemClass());
		T item = itemDao.getById(getItem().getId());
		getItemList().getReadWriteLock().writeLock().lock();
		try {
			int itemIndex = getItemList().indexOf(getItem());
			getItemList().set(itemIndex, item);
		} finally {
			getItemList().getReadWriteLock().writeLock().unlock();
		}
		message("endMessage", getItem().toString());
		return item;
	}
	
	private DaoService getDaoService() {
		return daoService;
	}

	private EventList<T> getItemList() {
		return itemList;
	}

	private T getItem() {
		return item;
	}

	private Class<T> getItemClass() {
		return itemClass;
	}
	
}
