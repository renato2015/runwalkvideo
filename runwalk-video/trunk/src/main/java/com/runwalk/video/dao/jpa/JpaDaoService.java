package com.runwalk.video.dao.jpa;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.common.collect.Maps;
import com.runwalk.video.dao.AbstractDaoService;
import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DataSourceBuilder;
import com.runwalk.video.settings.DatabaseSettings;

public class JpaDaoService extends AbstractDaoService {

	private final EntityManagerFactory entityManagerFactory;

	public JpaDaoService(DatabaseSettings databaseSettings, String applicationName) {
		// read db connection properties from settings file
		Map<String, Object> connectionProperties = Maps.newHashMap();
		DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(databaseSettings);
		/*connectionProperties.put("eclipselink.jdbc.url", dataSourceBuilder.buildJdbcUrl());
		connectionProperties.put("eclipselink.jdbc.user", databaseSettings.getUserName());
		connectionProperties.put("eclipselink.jdbc.password", databaseSettings.getPassword());*/
		connectionProperties.put("javax.persistence.nonJtaDataSource", dataSourceBuilder.build());
		// create entityManagerFactory for default persistence unit
		entityManagerFactory = Persistence.createEntityManagerFactory(applicationName, connectionProperties);
		addSpecializedDaos(entityManagerFactory);
	}
	
	/**
	 * Add specialized dao's to the service 
	 * @param entityManagerFactory The factory the dao's should use
	 */
	private void addSpecializedDaos(EntityManagerFactory entityManagerFactory) {
		addDao(new ClientDao(entityManagerFactory));
		addDao(new CityDao(entityManagerFactory));
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
