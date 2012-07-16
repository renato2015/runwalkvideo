package com.runwalk.video.settings;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class VideoCapturerSettings {

	private String monitorId;
	
	private String monitorResolution;
	
	private String captureEncodeName;
	
	private String name;
	
	public VideoCapturerSettings(String name) {
		this.name = name;
	}

	public VideoCapturerSettings(String name, String monitorId, String monitorResolution) {
		this.name = name;
		this.monitorId = monitorId;
		this.monitorResolution = monitorResolution;
	}
	
	public VideoCapturerSettings(String name, String monitorId, String monitorResolution,
			String captureEncoderName) {
		this(name, monitorId, monitorResolution);
		this.captureEncodeName = captureEncoderName;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEncoderName() {
		return captureEncodeName;
	}

	public void setCaptureEncoderName(String captureEncoderName) {
		this.captureEncodeName = captureEncoderName;
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

}
