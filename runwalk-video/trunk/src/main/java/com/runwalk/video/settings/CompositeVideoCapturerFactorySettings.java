package com.runwalk.video.settings;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import com.runwalk.video.media.CompositeVideoCapturerFactory;

@XmlRootElement
public class CompositeVideoCapturerFactorySettings extends VideoCapturerFactorySettings<VideoCapturerSettings> {
	
	@XmlElementRef
	private List<VideoCapturerFactorySettings<?>> videoCapturerFactorySettings;
	
	public CompositeVideoCapturerFactorySettings() {
		super(CompositeVideoCapturerFactory.class);
	}

	public List<VideoCapturerFactorySettings<?>> getVideoCapturerFactorySettings() {
		return videoCapturerFactorySettings;
	}

	public void setVideoCapturerFactorySettings(
			List<VideoCapturerFactorySettings<?>> videoCapturerFactorySettings) {
		this.videoCapturerFactorySettings = videoCapturerFactorySettings;
	}
	
}
