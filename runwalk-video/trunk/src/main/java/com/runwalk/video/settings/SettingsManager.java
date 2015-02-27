package com.runwalk.video.settings;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;
import org.jdesktop.beansbinding.ELProperty;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.collect.Lists;
import com.runwalk.video.io.DateVideoFolderRetrievalStrategy;
import com.runwalk.video.io.VideoFolderRetrievalStrategy;
import com.runwalk.video.media.VideoCapturerFactory;
import com.runwalk.video.media.settings.VideoComponentFactorySettings;
import com.runwalk.video.util.AppUtil;

public class SettingsManager {

	//FIXME dit zou terug uit een resourceMap moeten gehaald worden.
	public static Font MAIN_FONT = new Font("Geneva", Font.PLAIN, 11);  //= ApplicationUtil.getResourceMap(ApplicationSettings.class).getFont("Application.mainFont").deriveFont(11f);

	public final static String FILE_ENCODING = "UTF-8";

	public final static String SETTINGS_FILE_NAME = "settings.xml";

	private final static String FILE_APPENDER_NAME = "A1";

	private final static String UNCOMPRESSED_VIDEO_DIRNAME = "uncompressed";

	private static Logger logger;

	/** The settings directory is stored here */
	private final File localStorageDir;

	/** The settings file name is stored here */
	private final String settingsFileName;

	private transient JAXBContext jaxbContext;

	private File logFile;

	/** This object's fields will be mapped to XML using JaxB */
	private Settings settings;

	/** The parsed directories for both uncompressed and compressed video's are cached here after lazy initialization */
	private File videoDir, uncompressedVideoDir;

	/**
	 * This method will do the initial log4j configuration using the properties file embedded in the jar.
	 * You can reconfigure log4j afterwards by adding a log4j.properties file to the {@link #getLocalStorageDir()} directory.
	 * 
	 * This file will be loaded when {@link #loadSettings()} is executed.
	 */
	public static void configureLog4j() {
		URL resource = Thread.currentThread().getContextClassLoader().getResource("META-INF/log4j.properties");
		PropertyConfigurator.configure(resource);
		logger = Logger.getLogger(SettingsManager.class);
		FileAppender appndr = (FileAppender) Logger.getRootLogger().getAppender(FILE_APPENDER_NAME);
		logger.debug("Logging to file with location " + appndr.getFile());
		org.jdesktop.beansbinding.util.logging.Logger.getLogger(ELProperty.class.getName()).setLevel(Level.SEVERE);
	}

	public SettingsManager(File localStorageDir) {
		this(localStorageDir, SETTINGS_FILE_NAME);
	}

	public SettingsManager(File localStorageDir, String settingsFileName) {
		settings = new Settings();
		this.localStorageDir = localStorageDir;
		this.settingsFileName = settingsFileName;
		logger.debug("Scanning JAXB annotated classes ..");
		Class<?>[] classArray = findAnnotatedClasses();
		logger.debug("Instantiating JAXB context..");
		try {
			jaxbContext = JAXBContext.newInstance( classArray );
		} catch (JAXBException e) {
			logger.error("Exception while instantiating JAXB context", e);
		}
	}

	private Class<?>[] findAnnotatedClasses() {
		Class<?>[] result = null;
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//values/*[../../key/text()=\"" + XmlRootElement.class.getName() + "\"]";
		InputStream reflectionsOutput = this.getClass().getClassLoader().
				getResourceAsStream("META-INF/reflections/runwalk-video-reflections.xml");
		InputSource inputSource = new InputSource(reflectionsOutput);
		try {
			NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
			result = new Class<?>[nodes.getLength()];
			for (int i = 0; i < nodes.getLength(); i ++) {
				Node item = nodes.item(i);
				result[i] = Class.forName(item.getTextContent());
			}
		} catch (XPathExpressionException e) {
			logger.error("Exception while parsing xPath expression", e);
		} catch (ClassNotFoundException e) {
			logger.error("Exception while loading JAXB annotated class", e);
		} catch (DOMException e) {
			logger.error("Exception while loading JAXB annotated class", e);
		}
		return result;
	}

	public void loadSettings() {
		loadAddtionalLog4JSettings();
		logger.debug("Initializing ApplicationSettings..");
		File settingsFile = null;
		try { 
			settingsFile = createSettingsFileIfAbsent();
			if (settingsFile.length() > 0) {
				logger.debug("Loading application settings from file " + settingsFile.getAbsolutePath());
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				settings = (Settings) unmarshaller.unmarshal(settingsFile);
			}
		} catch(Exception exc) {
			logger.error("Exception thrown while loading settings file", exc);
			if (exc.getMessage() != null) {
				logger.error("Settings file seems to be corrupt. Attempting to delete..", exc);
				if (settingsFile != null) {
					settingsFile.delete();
					logger.warn("Settings file deleted. Default settings will be applied");
				}
			}
		} finally {
			synchronized(this) {
				if (settings == null) {
					settings = new Settings();
					saveSettings();
				}
			}
		}
		logger.debug("Found videodir: " + getVideoDir().getAbsolutePath());
		logger.debug("Found uncompressed videodir: " + getUncompressedVideoDir().getAbsolutePath());
	}
	
	/**
	 * Looks for additional log4j files in the user.home directory of the application.
	 * Use this to store specific appenders that require easy configuration. 
	 */
	private void loadAddtionalLog4JSettings() {
		File resource = new File(getLocalStorageDir(), "log4j.properties");
		if (resource.exists()) {
			logger.debug("Loading additional log4J properties from " + resource.getAbsolutePath());
			PropertyConfigurator.configure(resource.getAbsolutePath());
		}
	}

	public void saveSettings() {
		try {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, FILE_ENCODING);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			File settingsFile = createSettingsFileIfAbsent();
			logger.debug("Saving application settings to file " + settingsFile.getAbsolutePath());
			marshaller.marshal(settings, settingsFile);
		} catch (Exception exc) {
			logger.error("Exception thrown while saving settings to file " + settingsFileName, exc);
		} 
	}

	private File createSettingsFileIfAbsent() throws IOException {
		File settingsFile = new File(getLocalStorageDir(), settingsFileName);
		File settingsFolder = settingsFile.getParentFile();
		// check if parent folder and settings file exists, create them otherwise
		if (!((settingsFolder.exists() || settingsFolder.mkdirs()) && 
				(settingsFile.exists() || settingsFile.createNewFile()))) {
			throw new FileNotFoundException("Settings file could not be created");
		}
		return settingsFile;
	}

	public File getLocalStorageDir() {
		return localStorageDir;
	}

	public Settings getSettings() {
		return settings;
	}

	/**
	 * Set a new directory for storing and reading compressed video files. 
	 * The directory will be lazily reloaded by clearing the cached value.
	 * 
	 * @param dir The new directory
	 */
	public void setVideoDir(File dir) {
		getSettings().videoDir = dir.getAbsolutePath();
		// reset the cached value for this directory so it will be reinitialized during the next request
		videoDir = null;
	}

	public File getVideoDir() {
		synchronized(this) {
			if (videoDir == null) {
				File defaultDir = new File(System.getProperty("user.dir"));
				videoDir = getDirectory(getSettings().videoDir, defaultDir);
			}
		}
		return videoDir;
	}

	/**
	 * Set a new directory for storing and reading uncompressed video files. 
	 * The directory will be lazily reloaded by clearing the cached value.
	 * 
	 * @param dir The new directory
	 */
	public void setUncompressedVideoDir(File dir) {
		getSettings().uncompressedVideoDir = dir.getAbsolutePath();
		// reset the cached value for this directory so it will be reinitialized during the next request
		uncompressedVideoDir = null;
	}

	public File getUncompressedVideoDir() {
		synchronized(this) {
			if (uncompressedVideoDir == null) {
				File defaultDir = new File(getVideoDir(), UNCOMPRESSED_VIDEO_DIRNAME);
				uncompressedVideoDir = getDirectory(getSettings().uncompressedVideoDir, defaultDir);
			}
		}
		return uncompressedVideoDir;
	}

	public File getDirectory(String path, File defaultDir) {
		File result = null;
		// first check whether the path in the settings is null or not
		if (path == null) {
			// if null, use default layout and create
			result = defaultDir;
		} else {
			// if not null, then try to create directory
			File dir = new File(path);
			result = AppUtil.createDirectories(dir, defaultDir);
		}
		return result;
	}

	public File getLogFile() {
		if (logFile == null || !logFile.exists()) {
			FileAppender appndr = (FileAppender) Logger.getRootLogger().getAppender(FILE_APPENDER_NAME);
			String fileName = appndr.getFile();
			logFile = new File(fileName);
		}
		return logFile;
	}

	public String getTranscoderName() {
		return getSettings().transcoderName;
	}

	public String setTranscoderName(String transcoderName) {
		return getSettings().transcoderName = transcoderName;
	}

	public String getLogFileUploadUrl() {
		return getSettings().logFileUploadUrl;
	}

	public VideoFolderRetrievalStrategy getVideoFolderRetrievalStrategy() {
		return getSettings().videoFolderRetrievalStrategy;
	}

	public void setVideoFolderRetrievalStrategy(VideoFolderRetrievalStrategy videoFolderRetrievalStrategy) {
		getSettings().videoFolderRetrievalStrategy = videoFolderRetrievalStrategy;
	}

	public DatabaseSettings getDatabaseSettings() {
		return getSettings().databaseSettings;
	}

	public void setDatabaseSettings(DatabaseSettings databaseSettings) {
		getSettings().databaseSettings = databaseSettings;
	}

	public List<VideoComponentFactorySettings<?>> getVideoCapturerFactorySettings() {
		return getSettings().videoCapturerFactorySettings;
	}
	
	public void setVideoCapturerFactorySettings(List<VideoComponentFactorySettings<?>> videoCapturerFactorySettings) {
		getSettings().videoCapturerFactorySettings = Lists.newArrayList(videoCapturerFactorySettings);
	}
	
	public List<VideoComponentFactorySettings<?>> getVideoPlayerFactorySettings() {
		return getSettings().videoPlayerFactorySettings;
	}
	
	public void setVideoPlayerFactorySettings(List<VideoComponentFactorySettings<?>> videoPlayerFactorySettings) {
		getSettings().videoCapturerFactorySettings = Lists.newArrayList(videoPlayerFactorySettings);
	}
	
	public void setEmployeeId(Long employeeId) {
		getSettings().employeeId = employeeId;
	}
	
	public Long getEmployeeId() {
		return getSettings().employeeId;
	}
	
	public Long getLocationId() {
		return getSettings().locationId;
	}
	
	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Settings {
		
		private String transcoderName = "XviD MPEG-4 Codec";

		private String logFileUploadUrl = "http://your.upload.url";
		
		@XmlAnyElement(lax=true)
		@XmlElementWrapper(name="videoCapturerFactorySettingsList")
		private List<VideoComponentFactorySettings<?>> videoCapturerFactorySettings = Lists.newArrayList();
		
		@XmlAnyElement(lax=true)
		@XmlElementWrapper(name="videoPlayerFactorySettingsList")
		private List<VideoComponentFactorySettings<?>> videoPlayerFactorySettings = Lists.newArrayList();

		@XmlElementRef
		private VideoFolderRetrievalStrategy videoFolderRetrievalStrategy = new DateVideoFolderRetrievalStrategy("yyyy/MM - MMM/dd");
		// TODO eventually merge videoDir and uncompressedVideoDir in the strategy object? 
		private String videoDir;
		// TODO create a separate strategy object for uncompressed video's, too
		private String uncompressedVideoDir;
		
		private Long employeeId = 1L;
		
		private Long locationId = 1L;
		
		private DatabaseSettings databaseSettings = DatabaseSettings.JDBC_DEFAULT;
		
		{
			if (AppHelper.getPlatform() == PlatformType.WINDOWS) {
				videoDir = "D:\\Video's";
			} else if (AppHelper.getPlatform() == PlatformType.OS_X) {
				videoDir = System.getProperty("user.home")+ File.separator + "Movies";
			}
			// initialize factory settings with default values
			videoCapturerFactorySettings.add(VideoCapturerFactory.DEFAULT_SETTINGS);
		}

	}

}
