package com.runwalk.video.settings;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;

import com.runwalk.video.media.VideoCapturerFactory;

/**
 * This marshallable bean manages all the information required to build a {@link VideoCapturerFactory} instance.
 *
 * @author Jeroen Peelaerts
 */
@XmlRootElement
public class VideoCapturerFactorySettings<E extends VideoCapturerSettings> extends VideoComponentFactorySettings<E> {
	
	private String videoCapturerName;
	
	public VideoCapturerFactorySettings(Class<? extends VideoCapturerFactory<? extends VideoCapturerFactorySettings<E>>> videoCapturerFactoryClass) {
		super(videoCapturerFactoryClass.getSimpleName());
	}

	public VideoCapturerFactorySettings(String videoCapturerName,
			Class<? extends VideoCapturerFactory<? extends VideoCapturerFactorySettings<E>>> videoCapturerFactoryClass, 
			E... videoCapturerSettings) {
		super(videoCapturerFactoryClass, Arrays.asList(videoCapturerSettings));
		this.videoCapturerName = videoCapturerName;
	}
	
	public String getVideoCapturerName() {
		return videoCapturerName;
	}

	public void setVideoCapturerName(String videoCapturerName) {
		this.videoCapturerName = videoCapturerName;
	}
	
}
