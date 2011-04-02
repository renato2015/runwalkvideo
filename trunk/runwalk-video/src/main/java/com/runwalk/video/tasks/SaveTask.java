package com.runwalk.video.tasks;

import java.util.List;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.SerializableEntity;

public class SaveTask<T extends SerializableEntity<? super T>> extends AbstractTask<List<T>, Void> {

	private final EventList<T> itemList;

	private final DaoService daoService;

	private final Class<T> theClass;

	public SaveTask(DaoService daoService, Class<T> theClass, EventList<T> itemList) {
		super("save");
		this.itemList = GlazedLists.eventList(itemList);
		this.daoService = daoService;
		this.theClass = theClass;
	}

	protected List<T> doInBackground() {
		message("startMessage");
		// filter out the dirty items in the list
		Iterable<T> dirtyItems = Iterables.filter(getItemList(), new Predicate<T>() {

			public boolean apply(T input) {
				return input.isDirty();
			}

		});
		List<T> result = null;
		// advantage of dirty checking on the client is that we don't need to serialize the complete list for saving just a few items
		result = getDaoService().getDao(getTypeParameter()).merge(dirtyItems);
		// dirty flag should be set back to false by a task listener
		message("endMessage", result.size());
		return result;
	}

	private DaoService getDaoService() {
		return daoService;
	}

	private List<T> getItemList() {
		return itemList;
	}

	private Class<T> getTypeParameter() {
		return theClass;
	}

}