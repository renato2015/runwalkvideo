package com.runwalk.video.util;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdesktop.application.ApplicationContext;

import com.runwalk.video.RunwalkVideoApp;

@SuppressWarnings("serial")
public class ApplicationSettings implements Serializable {

	public final static Font MAIN_FONT = ApplicationUtil.getResourceMap(ApplicationSettings.class).getFont("Application.mainFont").deriveFont(11f);

	public final static String FILE_ENCODING = "UTF-8";
	
	private final static String FILE_APPENDER_NAME = "A1";
	
	private final static Logger LOGGER = Logger.getLogger(ApplicationSettings.class);
	
	private final static String CAMERADIR_RESOURCENAME = "Application.cameraDir";

	private final static String VIDEODIR_RESOURCENAME = "Application.videoDir";

	private final static String SETTINGS_XML = "settings.xml";

	private final static String TEMP_VIDEO_DIRNAME = "uncompressed";

	private static ApplicationSettings instance;

	private File logFile;

	private JAXBContext jaxbContext;
	
	private Settings settings;
	
	private ApplicationSettings() {
		try {
			LOGGER.debug("Instantiating JAXB context..");
			jaxbContext = JAXBContext.newInstance( Settings.class );
		} catch (JAXBException e) {
			LOGGER.error(e);
		}
	}

	public synchronized static ApplicationSettings getInstance() {
		if (instance == null) {
			instance = new ApplicationSettings();
		}
		return instance;
	}

	public void loadSettings() {
		LOGGER.debug("Initializing ApplicationSettings..");
		ApplicationContext appContext = RunwalkVideoApp.getApplication().getContext();
		try {
			File settingsFile = new File(appContext.getLocalStorage().getDirectory(), SETTINGS_XML);
			LOGGER.debug("Loading application settings from file " + settingsFile.getAbsolutePath());
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			settings = (Settings) unmarshaller.unmarshal(settingsFile);
		} catch(Exception exc) {
			LOGGER.error("Exception thrown while loading settings from file " + SETTINGS_XML, exc);
			if (exc.getMessage() != null) {
				LOGGER.error("Settings file " + SETTINGS_XML + " seems to be corrupt. Attempting to delete..", exc);
				try {
					appContext.getLocalStorage().deleteFile(SETTINGS_XML);
					LOGGER.warn("Settings file deleted. Default settings will be applied");
				} catch (IOException e) {
					LOGGER.error("Removing corrupt settings file failed", e);
				}
			}
		} finally {
			settings = settings == null ? new Settings() : settings;
		}
		Logger.getLogger(getClass()).debug("Found videodir: " + getVideoDir().getAbsolutePath());
	}

	public void saveSettings() {
		OutputStream fos = null;
		ApplicationContext appContext = RunwalkVideoApp.getApplication().getContext();
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, FILE_ENCODING);
			File settingsFile = new File(appContext.getLocalStorage().getDirectory(), SETTINGS_XML);
			fos = appContext.getLocalStorage().openOutputFile(settingsFile.getName());
			LOGGER.debug("Writing application settings to file " + settingsFile.getName());
			marshaller.marshal(settings, fos);
		} catch (Exception e) {
			LOGGER.error("Exception thrown while saving settings to file " + SETTINGS_XML, e);
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					LOGGER.error("Exception thrown while flushing or closing outputstream for file " + SETTINGS_XML, e);
				}
			}
		}
	}

	public void setVideoDir(File dir) {
		settings.setVideoDir(dir.getAbsolutePath());
	}

	public File getDirectory(String path, String resourceName) {
		File dir = ApplicationUtil.parseDir(path);
		if (path == null || dir == null) {
			String defaultDir = ApplicationUtil.getString(this, resourceName);
			dir = ApplicationUtil.parseDir(defaultDir);
			if (dir == null) {
				dir = new File(System.getProperty("user.dir"));
			}
		}
		return dir;
	}

	public File getCameraDir() {
		return getDirectory(settings.getCameraDir(), CAMERADIR_RESOURCENAME);
	}

	public File getVideoDir() {
		return getDirectory(settings.getVideoDir(), VIDEODIR_RESOURCENAME);
	}

	public File getUncompressedVideoDir() {
		File tempDir = new File(getVideoDir(), TEMP_VIDEO_DIRNAME);
		if (!ApplicationUtil.parseDir(tempDir)) {
			try {
				boolean success = tempDir.mkdir();
				if (success) {
					LOGGER.debug("Directory " + tempDir.getAbsolutePath() + " created.");
				}
			} catch(SecurityException excp) {
				LOGGER.error("Directory " + tempDir.getAbsolutePath() + " couldn't be created.", excp);
			}
		}
		return tempDir;
	}
	
	public File getLogFile() {
		if (logFile == null || !logFile.exists()) {
			FileAppender appndr = (FileAppender) Logger.getRootLogger().getAppender(FILE_APPENDER_NAME);
			String fileName = appndr.getFile();
			logFile = new File(fileName);
		}
		return logFile;
	}
	
	public static void configureLog4j() {
		PropertyConfigurator.configure(Thread.currentThread().getContextClassLoader().getResource("META-INF/log4j.properties"));
		FileAppender appndr = (FileAppender) Logger.getRootLogger().getAppender(FILE_APPENDER_NAME);
		LOGGER.debug("Logging to file with location " +appndr.getFile());
	}
	
	@XmlRootElement
	public static class Settings {
		@XmlElement
		private String videoDir, cameraDir;
		
		private String getVideoDir() {
			return videoDir;
		}
		
		private String getCameraDir() {
			return cameraDir;
		}

		private void setVideoDir(String videoDir) {
			this.videoDir = videoDir;
		}
	}
}
