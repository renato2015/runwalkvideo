package com.runwalk.video.gui.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Client;

public class SaveTask extends AbstractTask<Void, Void> {

	public SaveTask() {
		super("save");
	}

	@Override protected Void doInBackground() {
		message("startMessage");
		int numberOfClients = RunwalkVideoApp.getApplication().getClientTableModel().getItemCount();
		EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			List<Client> newList = new ArrayList<Client>(numberOfClients);
			for(int i = 0; i < numberOfClients; i ++) {
				Client client = RunwalkVideoApp.getApplication().getClientTableModel().getItem(i);
				Client mergedClient = em.merge(client);
				if (mergedClient == null) {
					newList = null;
					setProgress(numberOfClients, 0, numberOfClients);
					break;
				}
				newList.add(mergedClient);
				setProgress(i, 0, numberOfClients);
			}
			tx.commit();
			if (newList != null) {
				RunwalkVideoApp.getApplication().getClientTableModel().setItemList(newList);
			}
		} catch(Exception e) {
			logger.error("Exception thrown while saving item list.", e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			em.close();
		}
		
		return null;
	}
	
	@Override protected void finished() {
		RunwalkVideoApp.getApplication().getAnalysisTableModel().update();
		RunwalkVideoApp.getApplication().setSaveNeeded(false);
		message("endMessage");
	}
}