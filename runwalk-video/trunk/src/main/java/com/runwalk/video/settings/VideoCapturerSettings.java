package com.runwalk.video.settings;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class VideoCapturerSettings extends VideoComponentSettings {

	private String encoderName;
	
	public VideoCapturerSettings(String name) {
		this.name = name;
	}

	public VideoCapturerSettings(String name, String monitorId, String monitorResolution) {
		super(monitorId, monitorResolution);
		this.name = name;
	}
	
	public VideoCapturerSettings(String name, String monitorId, String monitorResolution,
			String encoderName) {
		this(name, monitorId, monitorResolution);
		this.encoderName = encoderName;
	}
	
	public String getEncoderName() {
		return encoderName;
	}

	public void setEncoderName(String encoderName) {
		this.encoderName = encoderName;
	}

}
