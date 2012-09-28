package com.runwalk.video.tasks;

import java.util.List;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.Matcher;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.SerializableEntity;

public class SaveTask<T extends SerializableEntity<? super T>> extends AbstractTask<List<T>, Void> {

	private final EventList<T> itemList;

	private final DaoService daoService;

	private final Class<T> theClass;

	public SaveTask(DaoService daoService, Class<T> theClass, EventList<T> itemList) {
		super("save");
		this.itemList = itemList;
		this.daoService = daoService;
		this.theClass = theClass;
	}

	protected List<T> doInBackground() {
		message("startMessage");
		List<T> result = null;
		getItemList().getReadWriteLock().readLock().lock();
		try {
			FilterList<T> dirtyItems = new FilterList<T>(getItemList(), new Matcher<T>() {

				public boolean matches(T item) {
					return item.isDirty();
				}
				
			});
			// advantage of dirty checking on the client is that we don't need to serialize the complete list for saving just a few items
			result = getDaoService().getDao(getTypeParameter()).merge(dirtyItems);
			message("endMessage", result.size());
		} finally {
			getItemList().getReadWriteLock().readLock().unlock();
		}
		// dirty flag should be set back to false by a task listener
		return result;
	}
	
	private DaoService getDaoService() {
		return daoService;
	}

	private EventList<T> getItemList() {
		return itemList;
	}

	private Class<T> getTypeParameter() {
		return theClass;
	}

}