package com.runwalk.video.media.settings;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class VideoPlayerSettings extends VideoComponentSettings {

	private float playRate;
	
	private String path;
	
	private float frameRate;

	public float getPlayRate() {
		return playRate;
	}

	public void setPlayRate(float playRate) {
		this.playRate = playRate;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public float getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(float frameRate) {
		this.frameRate = frameRate;
	}
	
}
