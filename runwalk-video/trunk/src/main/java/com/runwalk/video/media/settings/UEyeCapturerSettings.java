package com.runwalk.video.media.settings;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class UEyeCapturerSettings extends VideoCapturerSettings {
	
	/** Image quality [1 = lowest ... 100 = highest] */
	public static int DEFAULT_QUALITY = 75;

	private String settingsFile;
	
	private int cameraId;
	
	private int compressionQuality = DEFAULT_QUALITY;
	
	public UEyeCapturerSettings() {	}

	public UEyeCapturerSettings(String name, String settingsFile, String monitorId, String monitorResolution) {
		super(name, monitorId, monitorResolution);
	}

	public String getSettingsFile() {
		return settingsFile;
	}

	public void setSettingsFile(String settingsFile) {
		this.settingsFile = settingsFile;
	}

	public int getCameraId() {
		return cameraId;
	}

	public void setCameraId(int cameraId) {
		this.cameraId = cameraId;
	}

	public int getCompressionQuality() {
		return compressionQuality;
	}

	public void setCompressionQuality(int compressionQuality) {
		this.compressionQuality = compressionQuality;
	}
	
}
