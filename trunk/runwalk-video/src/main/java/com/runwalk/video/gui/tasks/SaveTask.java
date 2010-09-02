package com.runwalk.video.gui.tasks;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.runwalk.video.dao.DaoManager;
import com.runwalk.video.entities.SerializableEntity;

public class SaveTask<T extends SerializableEntity<T>> extends AbstractTask<List<T>, Void> {

	private List<T> itemList;

	private final DaoManager daoManager;

	private Class<T> theClass;

	public SaveTask(Class<T> theClass, List<T> itemList, DaoManager daoManager) {
		super("save");
		this.itemList = new ArrayList<T>(itemList);
		this.daoManager = daoManager;
		this.theClass = theClass;
	}

	@Override 
	protected List<T> doInBackground() {
		message("startMessage");
		// filter out the dirty items using the dirty flag
		Iterable<T> dirtyItems = Iterables.filter(getItemList(), new Predicate<T>() {

			public boolean apply(T input) {
				return input.isDirty();
			}

		});
		// find some neat way to discover the actual generic type at runtime here..
		List<?> asList = Arrays.asList(getClass().getTypeParameters());
//		TypeVariable<?> last = Iterables.getLast(asList);
		List<T> mergedList = getDaoManager().getDao(theClass).merge(dirtyItems);
		// advantage of dirty checking on the client is that we don't need to serialize the complete list for saving just a few items
		for(T item : mergedList) {
			int index = getItemList().indexOf(item);
			getItemList().set(index, item);
			item.setDirty(false);
		}
		message("endMessage");
		return mergedList;
	}

	private DaoManager getDaoManager() {
		return daoManager;
	}

	private List<T> getItemList() {
		return itemList;
	}

}