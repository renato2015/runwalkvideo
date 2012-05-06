package com.runwalk.video.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.google.common.collect.Iterables;
import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.SerializableEntity;

public class RefreshEntityTask<T extends SerializableEntity<? super T>> extends AbstractTask<List<T>, Void> {

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

	private long findMaxEntityId() {
		Comparator<T> idComparator = GlazedLists.beanPropertyComparator(getItemClass(), SerializableEntity.ID);
		ArrayList<T> sortedList = new ArrayList<T>(getItemList());
		Collections.sort(sortedList, idComparator);
		T lastItem = Iterables.getLast(sortedList);
		return lastItem.getId();
	}

	protected List<T> doInBackground() throws Exception {
		message("startMessage");
		List<T> result = null;
		Dao<T> itemDao = getDaoService().getDao(getItemClass());
		T selectedItem = itemDao.getById(getItem().getId());
		// get the last added entities
		result = itemDao.getNewEntities(findMaxEntityId());
		// add selected client to the end
		result.add(selectedItem);
		message("endMessage", result.size());
		return result;
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
