package com.runwalk.video.model;

import org.jdesktop.application.AbstractBean;

import com.runwalk.video.entities.SerializableEntity;

public class AbstractEntityModel<T extends SerializableEntity<? super T>> extends AbstractBean implements Comparable<AbstractEntityModel<T>> {
	
	public static final String DIRTY = "dirty";
	
	public static final String ENTITY = "entity";
	
	private T entity;
	
	private boolean dirty = false;

	public AbstractEntityModel(T entity) {
		this.entity = entity;
	}

	@Override
	public int hashCode() {
		return getEntity().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && getClass() == obj.getClass()) {
			AbstractEntityModel<?> abstractEntityModel = (AbstractEntityModel<?>) obj;
			return getEntity().equals(abstractEntityModel.getEntity());
		}
		return false;
	}
	
	@Override
	public int compareTo(AbstractEntityModel<T> o) {
		return getEntity().compareTo(o.getEntity());
	}
	
	public boolean isPersisted() {
		return getId() != null;
	}
	
	public T getEntity() {
		return entity;
	}
	
	public void setEntity(T entity) {
		// won't fire cuz most of the time identity is the same??
		firePropertyChange(ENTITY, this.entity, this.entity = entity);
	}

	public void setDirty(boolean dirty) {
		//firePropertyChange(DIRTY, this.dirty, this.dirty = dirty);
		this.dirty = dirty;
	}

	public boolean isDirty() {
		return this.dirty;
	}
	
	public Long getId() {
		return getEntity().getId();
	}

	
}
