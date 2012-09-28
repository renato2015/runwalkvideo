package com.runwalk.video.media.settings;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.runwalk.video.media.VideoComponentFactory;

@XmlRootElement
public class  VideoComponentFactorySettings<T extends VideoComponentSettings> {

	@XmlAnyElement(lax=true)
	private List<T> videoComponentSettings = new ArrayList<T>();
	
	private String videoComponentFactoryClassName;
	
	private String defaultVideoComponentName;
	
	public VideoComponentFactorySettings() { } 
	
	public VideoComponentFactorySettings(Class<? extends VideoComponentFactory<? extends T>> videoCapturerFactoryClass, 
			List<T> videoComponentSettings) {
		this(videoCapturerFactoryClass);
		this.videoComponentSettings = videoComponentSettings;
	}
	
	public VideoComponentFactorySettings(Class<? extends VideoComponentFactory<? extends T>> videoCapturerFactoryClass) {
		this(videoCapturerFactoryClass.getName());
	}

	public VideoComponentFactorySettings(String videoComponentFactoryClassName) {
		this.videoComponentFactoryClassName = videoComponentFactoryClassName;
	}

	public String getVideoComponentFactoryClassName() {
		return videoComponentFactoryClassName;
	}
	
	public void setVideoComponentFactoryClassName(String videoComponentFactoryClassName) {
		this.videoComponentFactoryClassName = videoComponentFactoryClassName;
	}

	public boolean addVideoComponentSettings(T videoCapturerSettings) {
		return videoComponentSettings.add(videoCapturerSettings);
	}

	public List<T> getVideoComponentSettings() {
		return videoComponentSettings;
	}

	public String getDefaultVideoComponentName() {
		return defaultVideoComponentName;
	}

	public void setDefaultVideoComponentName(String defaultVideoComponentName) {
		this.defaultVideoComponentName = defaultVideoComponentName;
	}
	
}
