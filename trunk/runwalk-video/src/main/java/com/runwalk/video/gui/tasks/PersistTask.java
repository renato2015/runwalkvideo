package com.runwalk.video.gui.tasks;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.util.AppUtil;

public class PersistTask<T extends SerializableEntity<T>> extends AbstractTask<Void, Void> {

	private final T item;
	
	private EntityManager entityManager;
	
	public PersistTask(T item, EntityManager entityManager) {
		super("persist");
		this.item = item;
		this.entityManager = entityManager;
	}

	protected Void doInBackground() {
		message("startMessage");
		EntityTransaction tx = null;
		try {
			tx = getEntityManager().getTransaction();
			tx.begin();
			getEntityManager().persist(item);
			tx.commit();
			message("endMessage");
			Logger.getLogger(AppUtil.class).debug(item.getClass().getSimpleName() + " with ID " + item.getId() + " was persisted.");
		} catch(Exception e) {
			Logger.getLogger(AppUtil.class).error("Exception thrown while persisting entity." , e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			getEntityManager().close();
		}
		return null;
	}
	
	private EntityManager getEntityManager() {
		return this.entityManager;
	}

}
