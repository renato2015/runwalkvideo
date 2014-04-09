package com.runwalk.video.glazedlists;

import java.util.Collections;
import java.util.List;

import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;

import org.jdesktop.application.TaskService;

import ca.odell.glazedlists.CollectionList;

import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.model.AbstractEntityModel;
import com.runwalk.video.tasks.AbstractTask;

/**
 * An extension of {@link CollectionList.Model} that will fetch a parent entity's property in a background thread.
 * Caching can be enabled only if the implementation of {@link #loadChildren(SerializableEntity)} returns the same
 * result after populating the entity's lazy associations.
 *
 * @param <E> The parent entity that owns the property collection to be loaded
 * @param <S> The type of the child entity
 */
public abstract class LazyCollectionModel<P extends SerializableEntity<? super P>, C extends SerializableEntity<C>, PM extends AbstractEntityModel<P>, CM extends AbstractEntityModel<C>> implements CollectionList.Model<PM, CM> {

	private final String attributeName;
	
	private final TaskService taskService;
	
	public LazyCollectionModel(TaskService taskService, String attributeName) {
		this.taskService = taskService;
		this.attributeName = attributeName;
	}
	
	public abstract List<CM> getLoadedChildren(PM parentModel);
	
	public abstract List<C> loadChildren(P parent);
	
	public abstract void refreshParent(PM parentModel, List<C> children);
	
	public boolean isLoaded(PM parentModel) {
		PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
		return !parentModel.isPersisted() || persistenceUtil.isLoaded(parentModel.getEntity(), getAttributeName());
	}
	
	public List<CM> getChildren(final PM parentModel) {
		List<CM> result = Collections.emptyList();
		if (isLoaded(parentModel)) {
			result = getLoadedChildren(parentModel);
		} else {
			final P parent = parentModel.getEntity();
			getTaskService().execute(new AbstractTask<List<C>, Void>("loadEntities") {
				
				protected List<C> doInBackground() throws Exception {
					List<C> result = loadChildren(parent);
					message("endMessage", parent);
					return result;
				}
				
				@Override
				protected void succeeded(List<C> children) {
					// execute callback and refresh the parent list
					refreshParent(parentModel, children);
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