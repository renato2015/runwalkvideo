package com.runwalk.video.gui.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Level;

import com.runwalk.video.entities.SerializableEntity;

public class SaveTask<T extends SerializableEntity<T>> extends AbstractTask<List<T>, Void> {
	
	private List<T> itemList;
	
	private EntityManager entityManager;

	public SaveTask(List<T> itemList, EntityManager entityManager) {
		super("save");
		this.itemList = new ArrayList<T>(itemList);
		this.entityManager = entityManager;
	}

	@Override 
	protected List<T> doInBackground() {
		message("startMessage");
		int listSize = getItemList().size();
		List<T> mergedList = new ArrayList<T>(listSize);
		EntityTransaction tx = null;
		try {
			tx = getEntityManager().getTransaction();
			tx.begin();
			for(int i = 0; i < listSize; i ++) {
				T item = getItemList().get(i);
				if (item.isDirty()) {
					getLogger().log(Level.INFO, "Saving " + item.toString());
					T mergedItem = getEntityManager().merge(item);
					if (mergedItem == null) {
						mergedList = null;
						setProgress(listSize, 0, listSize);
						break;
					}
					item.setDirty(false);
					mergedList.add(mergedItem);
				}
				setProgress(i, 0, listSize);
			}
			// actual updating is done here!
			tx.commit();
			message("endMessage");
		} catch(Exception e) {
			getLogger().error("Exception thrown while saving item list.", e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			getEntityManager().close();
		}
		return mergedList;
	}

	private EntityManager getEntityManager() {
		return entityManager;
	}
	
	private List<T> getItemList() {
		return itemList;
	}
	
}