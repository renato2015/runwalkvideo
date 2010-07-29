package com.runwalk.video;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdesktop.application.AbstractBean;

import com.google.common.collect.Maps;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

import de.humatic.dsj.DSJException;

/**
 * This manager caches the recordings together with their corresponding video files.
 * It performs some simple file checking at data loading time to see whether the files actually exist.
 * This file checking logic was previously encapsulated in the {@link Recording} bean itself, 
 * which was a bad design choice when the idea came up to create a server component for the application, 
 * as the files are only locally accessible from each client's file system.
 * 
 * @author Jeroen Peelaerts
 *
 */
public class VideoFileManager extends AbstractBean {

	private Map<Recording, File> recordingFileMap = Maps.newHashMap();

	public File getVideoFile(Recording recording) {
		File videoFile = recordingFileMap.get(recording);
		if (videoFile == null || !canReadAndExists(videoFile))  {
			File compressedVideoFile = getCompressedVideoFile(recording);
			File uncompressedVideoFile = getUncompressedVideoFile(recording);
			if (canReadAndExists(compressedVideoFile)) {
				recording.setRecordingStatus(RecordingStatus.COMPRESSED);
				// duration wasn't saved.. set again
				videoFile = compressedVideoFile;
				if (recording.getDuration() == 0) {
					recording.setDuration(getDuration(videoFile));
				}
			} else if (canReadAndExists(uncompressedVideoFile)) {
				recording.setRecordingStatus(RecordingStatus.UNCOMPRESSED);
				// duration wasn't saved.. set again
				videoFile = uncompressedVideoFile;
				if (recording.getDuration() == 0) {
					recording.setDuration(getDuration(videoFile));
				}
			} else if (!recording.isRecorded()) {
				Logger.getLogger(Recording.class).warn("No videofile found for recording with filename " + recording.getVideoFileName());
				recording.setRecordingStatus(RecordingStatus.NON_EXISTANT_FILE);
			} else {
				recording.setRecordingStatus(RecordingStatus.READY);
				videoFile = uncompressedVideoFile;
			}
			recordingFileMap.put(recording, videoFile);
		}
		return videoFile;
	}
	
	public void clear() {
		recordingFileMap.clear();
	}

	public boolean canReadAndExists(File videoFile) {
		return videoFile != null && videoFile.exists() && videoFile.canRead();
	}

	public File getCompressedVideoFile(Recording recording) {
		return new File(AppSettings.getInstance().getVideoDir(), recording.getVideoFileName());
	}

	public File getUncompressedVideoFile(Recording recording) {
		return new File(AppSettings.getInstance().getUncompressedVideoDir(), recording.getVideoFileName());
	}

	public long getDuration(File videoFile) throws DSJException  {
		long duration = 0;
		try {
			duration = AppUtil.getMovieDuration(videoFile.getAbsolutePath());
		} catch(DSJException e) {
			String hresultToHexString = DSJException.hresultToHexString(e.getErrorCode());
			Logger.getLogger(getClass()).error("Failed to read meta info from " + getClass().getSimpleName() +  
					" with name " + videoFile.getName() + " (hex code: 0x" + hresultToHexString + ")", e);
		}
		return duration;
	}

	public boolean hasDuplicateFiles(Recording recording) {
		return getCompressedVideoFile(recording).exists() && getUncompressedVideoFile(recording).exists();
	}

	public void deleteVideoFile(Recording recording) {
		File videoFile = getVideoFile(recording);
		if (videoFile != null && !videoFile.delete()) {
			Logger.getLogger(VideoFileManager.class).warn(videoFile.getAbsolutePath() + " could not be deleted.");
		}
	}

}
