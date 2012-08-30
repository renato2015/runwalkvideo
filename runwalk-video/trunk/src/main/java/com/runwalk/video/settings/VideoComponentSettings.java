package com.runwalk.video.settings;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class VideoComponentSettings {

	private String monitorId;
	
	private String monitorResolution;

	protected String name;
	
	public VideoComponentSettings() { }

	public VideoComponentSettings(String monitorId, String monitorResolution) {
		this.monitorId = monitorId;
		this.monitorResolution = monitorResolution;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorResolution() {
		return monitorResolution;
	}

	public void setMonitorResolution(String monitorResolution) {
		this.monitorResolution = monitorResolution;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
