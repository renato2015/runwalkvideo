package com.runwalk.video.entities;

import java.io.File;

import org.apache.log4j.Logger;

import com.runwalk.video.util.ApplicationSettings;
import com.runwalk.video.util.ApplicationUtil;

import de.humatic.dsj.DSJException;

@SuppressWarnings("serial")
public abstract class VideoFile extends File {

	private int duration;
	
	protected VideoFile(File file, String name) {
		super(file, name);
	}

	int getDuration() throws DSJException  {
		if (duration == 0) {
			duration = ApplicationUtil.getMovieDuration(getAbsolutePath());
		}
		return duration;
	}
	
	public boolean canReadAndExists() {
		return exists() && canRead();
	}
	
	@Override
	public boolean canRead() {
		boolean canRead = super.canRead();
		try {
			canRead = canRead && getDuration() != 0;
		} catch(DSJException e) {
			String hresultToHexString = DSJException.hresultToHexString(e.getErrorCode());
			Logger.getLogger(getClass()).error("Failed to read meta info from " + getClass().getSimpleName() +  " with name " + getName() + " (hex error code: " + hresultToHexString + ")", e);
			canRead = false;
		}
		return canRead;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [duration=" + duration + ", file=" + getAbsolutePath() + "]";
	}
	
	
	@SuppressWarnings("serial")
	public static class CompressedVideoFile extends VideoFile {

		CompressedVideoFile(String fileName) {
			super(ApplicationSettings.getInstance().getVideoDir(), fileName);
		}
		
	}
	
	@SuppressWarnings("serial")
	public static class UncompressedVideoFile extends VideoFile {

		UncompressedVideoFile(String fileName) {
			super(ApplicationSettings.getInstance().getUncompressedVideoDir(), fileName);
		}
		
	}
	
}
