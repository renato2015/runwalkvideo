package com.runwalk.video.media.ueye;

import com.runwalk.video.settings.VideoCapturerSettings;

public class UEyeCapturerSettings extends VideoCapturerSettings {

	private String settingsFile;

	public UEyeCapturerSettings(String name, String settingsFile, String monitorId, String monitorResolution) {
		super(name, monitorId, monitorResolution);
	}

	public String getSettingsFile() {
		return settingsFile;
	}

	public void setSettingsFile(String settingsFile) {
		this.settingsFile = settingsFile;
	}
	
}
