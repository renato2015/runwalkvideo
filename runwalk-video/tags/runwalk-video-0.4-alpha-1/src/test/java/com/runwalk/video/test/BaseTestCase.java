package com.runwalk.video.test;

import java.io.File;

import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.runwalk.video.settings.SettingsManager;

import junit.framework.TestCase;

/**
 * Just a simple base {@link TestCase} containing some common setup logic.
 * 
 * @author Jeroen Peelaerts
 *
 */
public class BaseTestCase extends TestCase {
	
	public static final String SETTINGS_FILE_NAME = "settings.text.xml";
	
	private File storageDir;
	
	private SettingsManager settingsManager;
	
	public void setUp() {
		SettingsManager.configureLog4j();
		// configure storageDir according to detected platform
		if (AppHelper.getPlatform() == PlatformType.WINDOWS) {
			storageDir = new File("C:\\Documents And Settings\\Application Data\\Runwalk Herentals\\Runwalk");
		} else if (AppHelper.getPlatform() == PlatformType.OS_X)  {
			storageDir = new File("/Users/jekkos/Library/Application Support/Runwalk Herentals");
		}
		settingsManager = new SettingsManager(storageDir, SETTINGS_FILE_NAME);
	}
	
	protected SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public File getStorageDir() {
		return storageDir;
	}
	
}
