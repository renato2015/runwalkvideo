package com.runwalk.video.settings;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DatabaseSettings extends AuthenticationSettings {
	
	public static final String DEFAULT_DRIVER = "com.mysql.jdbc.Driver";
	public static final int DEFAULT_POOL_SIZE = 3;
	public static final int DEFAULT_MIN_POOL_SIZE = 1;
	public static final int DEFAULT_MAX_POOL_SIZE = 4;
	public static final int DEFAULT_IDLE_TEST_PERIOD = 0;
	public static final int DEFAULT_MAX_IDLE_TIME = 29;
	
	public static final DatabaseSettings JDBC_DEFAULT = 
			new DatabaseSettings("root", "password", "jdbc:mysql://localhost:3306");

	private String driverClass = DEFAULT_DRIVER;
	private int initalPoolSize = DEFAULT_POOL_SIZE;
	private int minPoolSize = DEFAULT_MIN_POOL_SIZE;
	private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
	private int idleConnectionTestPeriod = DEFAULT_IDLE_TEST_PERIOD;
	private int maxIdleTime = DEFAULT_MAX_IDLE_TIME;
	
	public DatabaseSettings() {	}

	public DatabaseSettings(String userName, String password, String url) {
		super(userName, password, url);
	}

	public String getDriverClass() {
		return driverClass;
	}
	
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}
	
	public int getInitalPoolSize() {
		return initalPoolSize;
	}
	
	public void setInitalPoolSize(int initalPoolSize) {
		this.initalPoolSize = initalPoolSize;
	}
	
	public int getMinPoolSize() {
		return minPoolSize;
	}
	
	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}
	
	public int getMaxPoolSize() {
		return maxPoolSize;
	}
	
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getIdleConnectionTestPeriod() {
		return idleConnectionTestPeriod;
	}

	public void setIdleConnectionTestPeriod(int idleConnectionTestPeriod) {
		this.idleConnectionTestPeriod = idleConnectionTestPeriod;
	}

	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}
	
}
