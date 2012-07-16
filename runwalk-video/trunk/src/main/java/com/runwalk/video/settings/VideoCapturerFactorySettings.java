package com.runwalk.video.settings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import com.runwalk.video.media.VideoCapturerFactory;

/**
 * This marshallable bean manages all the information required to build a {@link VideoCapturerFactory} instance.
 *
 * @author Jeroen Peelaerts
 */
@XmlRootElement
public class VideoCapturerFactorySettings {
	
	@XmlElementRef
	private List<VideoCapturerSettings> videoCapturerSettingsList;
	
	private String videoCapturerName;
	
	private final String videoCapturerFactoryClassName;
	
	public VideoCapturerFactorySettings(Class<? extends VideoCapturerFactory> videoCapturerFactoryClass) {
		this.videoCapturerFactoryClassName = videoCapturerFactoryClass.getSimpleName();
	}

	public VideoCapturerFactorySettings(String videoCapturerName, Class<? extends VideoCapturerFactory> videoCapturerFactoryClass, 
			VideoCapturerSettings... videoCapturerSettings) {
		this(videoCapturerFactoryClass);
		this.videoCapturerSettingsList = Arrays.asList(videoCapturerSettings);
		this.videoCapturerName = videoCapturerName;
	}

	public List<VideoCapturerSettings> getVideoCapturerSettingsList() {
		return Collections.unmodifiableList(videoCapturerSettingsList);
	}
	
	public boolean addVideoCapturerSettings(VideoCapturerSettings videoCapturerSettings) {
		return videoCapturerSettingsList.add(videoCapturerSettings);
	}
	
	public String getVideoCapturerName() {
		return videoCapturerName;
	}

	public void setVideoCapturerName(String videoCapturerName) {
		this.videoCapturerName = videoCapturerName;
	}

	public String getVideoCapturerFactoryClassName() {
		return videoCapturerFactoryClassName;
	}

}
