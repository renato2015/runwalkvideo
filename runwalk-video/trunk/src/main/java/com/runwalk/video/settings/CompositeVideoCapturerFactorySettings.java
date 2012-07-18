package com.runwalk.video.settings;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import com.runwalk.video.media.CompositeVideoCapturerFactory;

@XmlRootElement
public class CompositeVideoCapturerFactorySettings extends VideoCapturerFactorySettings<VideoCapturerSettings> {
	
	@XmlElementRef
	private List<VideoCapturerFactorySettings<? extends VideoCapturerSettings>> videoCapturerFactorySettings;
	
	public CompositeVideoCapturerFactorySettings() {
		super(CompositeVideoCapturerFactory.class);
	}

	public List<VideoCapturerFactorySettings<? extends VideoCapturerSettings>> getVideoCapturerFactorySettings() {
		return videoCapturerFactorySettings;
	}

	public void setVideoCapturerFactorySettings(
			List<VideoCapturerFactorySettings<? extends VideoCapturerSettings>> videoCapturerFactorySettings) {
		this.videoCapturerFactorySettings = videoCapturerFactorySettings;
	}
	
}
