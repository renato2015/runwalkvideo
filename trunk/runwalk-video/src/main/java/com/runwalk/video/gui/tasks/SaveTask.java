package com.runwalk.video.gui.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.SerializableEntity;

public class SaveTask<T extends SerializableEntity<T>> extends AbstractTask<List<T>, Void> {
	
	private List<T> itemList;

	public SaveTask(List<T> itemList) {
		super("save");
		this.itemList = itemList;
	}

	@Override 
	protected List<T> doInBackground() {
		message("startMessage");
		int listSize = itemList.size();
		List<T> newList = new ArrayList<T>(listSize);
		EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			for(int i = 0; i < listSize; i ++) {
				T item = itemList.get(i);
				T mergedItem = em.merge(item);
				if (mergedItem == null) {
					newList = null;
					setProgress(listSize, 0, listSize);
					break;
				}
				newList.add(mergedItem);
				setProgress(i, 0, listSize);
			}
			tx.commit();
		} catch(Exception e) {
			getLogger().error("Exception thrown while saving item list.", e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			em.close();
		}
		return newList;
	}
	
	@Override 
	protected void finished() {
		message("endMessage");
	}
}