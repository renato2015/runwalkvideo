package com.runwalk.video.settings;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class VideoCapturerSettings extends VideoComponentSettings {

	private String captureEncodeName;
	
	private String name;
	
	public VideoCapturerSettings(String name) {
		this.name = name;
	}

	public VideoCapturerSettings(String name, String monitorId, String monitorResolution) {
		super(monitorId, monitorResolution);
		this.name = name;
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

}
