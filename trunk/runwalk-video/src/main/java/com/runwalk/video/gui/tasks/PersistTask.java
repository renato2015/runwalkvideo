package com.runwalk.video.gui.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.util.AppUtil;

public class PersistTask<T extends SerializableEntity<T>> extends AbstractTask<Void, Void> {

	private final T item;
	
	public PersistTask(T item) {
		super("persist");
		this.item = item;
	}

	protected Void doInBackground() {
		message("startMessage");
		EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			em.persist(item);
			tx.commit();
			message("endMessage");
			Logger.getLogger(AppUtil.class).debug(item.getClass().getSimpleName() + " with ID " + item.getId() + " was persisted.");
		} catch(Exception e) {
			Logger.getLogger(AppUtil.class).error("Exception thrown while persisting entity." , e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			em.close();
		}
		return null;
	}

}
