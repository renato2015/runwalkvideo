package com.runwalk.video.dao.jpa;

import javax.persistence.EntityManagerFactory;

import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.RedcordSession;

public class RedcordSessionDao extends CalendarSlotDao<RedcordSession> {

	public RedcordSessionDao(EntityManagerFactory entityManagerFactory) {
		super(RedcordSession.class, entityManagerFactory);
	}

	@Override
	public void persist(RedcordSession item) {
		super.persist(item);
		Client client = item.getClient();
		if (!client.getRedcordSessions().contains(item)) {
			client.addRedcordSession(item);
		}
	}
	
	
}
