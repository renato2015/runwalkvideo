package com.runwalk.video.dao;

import java.beans.PropertyVetoException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.runwalk.video.settings.DatabaseSettings;

public class DataSourceBuilder {

	private DatabaseSettings databaseSettings;
	
	private static final String UTF8 = "utf8";
	private static final String UTF8_GENERAL_CI = "utf8_general_ci";
	private static final String CHARACTER_SET_RESULTS = "characterSetResults";
	private static final String CONNECTION_COLLATION = "connectionCollation";
	private static final String USE_UNICODE = "useUnicode";
	private static final String ZERO_TIME_BEHAVIOR = "zeroDateTimeBehavior";
	private static final String CONVERT_TO_NULL = "convertToNull";
	
	private static final Map<String, String> DB_OPTIONS;
	
	static {
		Builder<String, String> builder = ImmutableMap.builder();
		builder.put(USE_UNICODE, Boolean.TRUE.toString());
		builder.put(CONNECTION_COLLATION, UTF8_GENERAL_CI);
		builder.put(CHARACTER_SET_RESULTS, UTF8);
		builder.put(ZERO_TIME_BEHAVIOR, CONVERT_TO_NULL);
		DB_OPTIONS = builder.build();
	};

	public DataSourceBuilder(DatabaseSettings databaseSettings) {
		this.databaseSettings = databaseSettings;
	}
	
	public DataSource build() {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setMinPoolSize(databaseSettings.getMinPoolSize());
			dataSource.setMaxPoolSize(databaseSettings.getMaxPoolSize());
			dataSource.setInitialPoolSize(databaseSettings.getInitalPoolSize());
			dataSource.setMaxIdleTime(databaseSettings.getMaxIdleTime());
			dataSource.setDriverClass(databaseSettings.getDriverClass());
			dataSource.setJdbcUrl(buildJdbcUrl(DB_OPTIONS));
			dataSource.setUser(databaseSettings.getUserName());
			dataSource.setPassword(databaseSettings.getPassword());
			//dataSource.setIdleConnectionTestPeriod(databaseSettings.getIdleConnectionTestPeriod());
		} catch(PropertyVetoException e) {
			Logger.getLogger(DataSourceBuilder.class).error(e);
		}
		return dataSource;
	}
	
	public String buildJdbcUrl() {
		return buildJdbcUrl(DB_OPTIONS);
	}

	public String buildJdbcUrl(Map<String, String> options) {
		Joiner.MapJoiner joiner = Joiner.on("&amp;").withKeyValueSeparator("=");
		return databaseSettings.getUrl() + "?" + joiner.join(options);
	}
	
}
