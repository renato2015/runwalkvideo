package com.runwalk.video.dao.jpa;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.common.collect.Maps;
import com.runwalk.video.dao.AbstractDaoService;
import com.runwalk.video.dao.Dao;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.RedcordSession;
import com.runwalk.video.settings.AuthenticationSettings;

public class JpaDaoService extends AbstractDaoService {

	private final EntityManagerFactory entityManagerFactory;

	public JpaDaoService(AuthenticationSettings authenticationSettings, String applicationName) {
		// read db connection properties from settings file
		Map<String, String> connectionProperties = Maps.newHashMap();
		connectionProperties.put("eclipselink.jdbc.url", authenticationSettings.getUrl());
		connectionProperties.put("eclipselink.jdbc.user", authenticationSettings.getUserName());
		connectionProperties.put("eclipselink.jdbc.password", authenticationSettings.getPassword());
		// create entityManagerFactory for default persistence unit
		entityManagerFactory = Persistence.createEntityManagerFactory(applicationName, connectionProperties);
		addSpecializedDaos(entityManagerFactory);
	}
	
	/**
	 * Add specialized dao's to the service 
	 * @param entityManagerFactory The factory the dao's should use
	 */
	private void addSpecializedDaos(EntityManagerFactory entityManagerFactory) {
		addDao(new ClientDao(Client.class, entityManagerFactory));
		// add a generic dao 
		addDao(new CalendarSlotDao<RedcordSession>(RedcordSession.class, entityManagerFactory));
	}

	@SuppressWarnings("unchecked")
	public <E, D extends Dao<E>> D getDao(Class<E> type) {
		synchronized(this) {
			D result = (D) getDaos().get(type);
			if (result == null) {
				if (type.getAnnotation(Entity.class) != null) {
					result = (D) new JpaDao<E>(type, entityManagerFactory);
					getDaos().put(type, result);
				}
			}
			return result;
		}
	}

	public void shutdown() {
		super.shutdown();
		entityManagerFactory.close();
	}

}
