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
 * Caching can be enabled only if the implementation of {@link #loadChildren(SerializableEntity)} returns the same
 * result after populating the entity's lazy associations.
 *
 * @param <E> The parent entity that owns the property collection to be loaded
 * @param <S> The type of the child entity
 */
public abstract class LazyCollectionModel<E extends SerializableEntity<? super E>, S> implements CollectionList.Model<E, S> {

	private final String attributeName;
	
	private final TaskService taskService;
	
	public LazyCollectionModel(TaskService taskService, String attributeName) {
		this.taskService = taskService;
		this.attributeName = attributeName;
	}
	
	public abstract List<S> getLoadedChildren(E parent);
	
	public abstract List<S> loadChildren(E parent);
	
	public abstract void refreshParent(E parent, List<S> children);
	
	public boolean isLoaded(E parent) {
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		return !parent.isPersisted() || persistenceUtil.isLoaded(parent, getAttributeName());
	}
	
	public List<S> getChildren(final E parent) {
		List<S> result = Collections.emptyList();
		if (isLoaded(parent)) {
			result = getLoadedChildren(parent);
		} else {
			getTaskService().execute(new AbstractTask<List<S>, Void>("loadEntities") {
				
				protected List<S> doInBackground() throws Exception {
					List<S> result = loadChildren(parent);
					message("endMessage", parent);
					return result;
				}
				
				@Override
				protected void succeeded(List<S> children) {
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