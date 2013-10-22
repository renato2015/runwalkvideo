package com.runwalk.video.dao.jpa;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import com.runwalk.video.dao.AbstractDaoService;
import com.runwalk.video.dao.Dao;
import com.runwalk.video.entities.RedcordSession;
import com.runwalk.video.settings.AuthenticationSettings;

public class JpaDaoService extends AbstractDaoService {

	private static final String UTF8 = "utf8";
	private static final String UTF8_GENERAL_CI = "utf8_general_ci";
	private static final String CHARACTER_SET_RESULTS = "characterSetResults";
	private static final String CONNECTION_COLLATION = "connectionCollation";
	private static final String USE_UNICODE = "useUnicode";
	
	private static final Map<String, String> DB_OPTIONS;
	
	static {
		Builder<String, String> builder = ImmutableMap.builder();
		builder.put(USE_UNICODE, Boolean.TRUE.toString());
		builder.put(CONNECTION_COLLATION, UTF8_GENERAL_CI);
		builder.put(CHARACTER_SET_RESULTS, UTF8);
		DB_OPTIONS = builder.build();
	};
	
	private final EntityManagerFactory entityManagerFactory;

	public JpaDaoService(AuthenticationSettings authenticationSettings, String applicationName) {
		// read db connection properties from settings file
		Map<String, String> connectionProperties = Maps.newHashMap();
		Joiner.MapJoiner joiner = Joiner.on("&amp;").withKeyValueSeparator("=");
		connectionProperties.put("eclipselink.jdbc.url", authenticationSettings.getUrl() + "?" + joiner.join(DB_OPTIONS));
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
		addDao(new ClientDao(entityManagerFactory));
		addDao(new CityDao(entityManagerFactory));
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
