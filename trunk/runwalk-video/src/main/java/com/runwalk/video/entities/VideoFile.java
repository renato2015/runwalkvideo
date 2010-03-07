package com.runwalk.video.entities;

import java.io.File;

import org.apache.log4j.Logger;

import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

import de.humatic.dsj.DSJException;

@SuppressWarnings("serial")
public abstract class VideoFile extends File {

	private int duration;
	
	protected VideoFile(File file, String name) {
		super(file, name);
	}

	/* (non-Javadoc)
	 * @see com.runwalk.video.entities.VideoFile#getDuration()
	 */
	public int getDuration() throws DSJException  {
		if (duration == 0) {
			duration = AppUtil.getMovieDuration(getAbsolutePath());
		}
		return duration;
	}
	
	/* (non-Javadoc)
	 * @see com.runwalk.video.entities.VideoFile#canReadAndExists()
	 */
	public boolean canReadAndExists() {
		return exists() && canRead();
	}
	
	/* (non-Javadoc)
	 * @see com.runwalk.video.entities.VideoFile#canRead()
	 */
	@Override
	public boolean canRead() {
		boolean canRead = super.canRead();
		try {
			canRead = canRead && getDuration() != 0;
		} catch(DSJException e) {
			String hresultToHexString = DSJException.hresultToHexString(e.getErrorCode());
			Logger.getLogger(getClass()).error("Failed to read meta info from " + getClass().getSimpleName() +  " with name " + getName() + " (hex code: 0x" + hresultToHexString + ")", e);
			canRead = false;
		}
		return canRead;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [duration=" + duration + ", file=" + getAbsolutePath() + "]";
	}
	
	
	public static class CompressedVideoFile extends VideoFile {

		CompressedVideoFile(String fileName) {
			super(AppSettings.getInstance().getVideoDir(), fileName);
		}
		
	}
	
	public static class UncompressedVideoFile extends VideoFile {

		UncompressedVideoFile(String fileName) {
			super(AppSettings.getInstance().getUncompressedVideoDir(), fileName);
		}
		
	}
	
}
