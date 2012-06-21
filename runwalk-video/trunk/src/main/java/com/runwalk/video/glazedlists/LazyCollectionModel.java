package com.runwalk.video.glazedlists;

import java.util.Collections;
import java.util.List;

import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;

import org.jdesktop.application.TaskService;

import ca.odell.glazedlists.CollectionList;

import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.tasks.AbstractTask;

/**
 * An extension of {@link CollectionList.Model} that will fetch a parent entity's property in a background thread.
 * Caching can be enabled only if the implementation of {@link #getLazyChildren(SerializableEntity)} returns the same
 * result after populating the entity's lazy associations.
 *
 * @param <E> The parent entity that owns the property collection to be loaded
 * @param <S> The type of the child entity
 */
public abstract class LazyCollectionModel<E extends SerializableEntity<? super E>, S> implements CollectionList.Model<E, S> {

	private final String attributeName;
	
	private final TaskService taskService;
	
	private List<S> cachedList;
	
	private final boolean useCache;
	
	public LazyCollectionModel(TaskService taskService, String attributeName, boolean useCache) {
		this.taskService = taskService;
		this.attributeName = attributeName;
		this.useCache = useCache;
	}
	
	public abstract List<S> getLazyChildren(E parent);
	
	public abstract void refreshParent(E parent, List<S> children);	
	
	public List<S> getChildren(final E parent) {
		List<S> result = Collections.emptyList();
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		if (persistenceUtil.isLoaded(parent, getAttributeName())) {
			if (useCache && cachedList != null) {
				result = cachedList;
				cachedList = null;
			} else {
				result = getLazyChildren(parent);
			}
		} else {
			getTaskService().execute(new AbstractTask<List<S>, Void>("loadEntities") {
				
				protected List<S> doInBackground() throws Exception {
					List<S> result = getLazyChildren(parent);
					message("endMessage", parent);
					return result;
				}
				
				@Override
				protected void succeeded(List<S> children) {
					if (useCache) {
						// cache the fetched result
						cachedList = children;
					}
					// execute callback and refresh the parent list
					refreshParent(parent, children);
				}
				
			});
		}
		return result;
	}
	
	public String getAttributeName() {
		return attributeName;
	}

	public TaskService getTaskService() {
		return taskService;
	}
	
}