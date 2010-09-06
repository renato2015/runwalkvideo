package com.runwalk.video;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdesktop.application.AbstractBean;

import com.google.common.collect.Maps;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.util.AppSettings;

import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSJUtils;

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
	
	private VideoFolderRetrievalStrategy folderRetrievalStrategy;
	
	private final AppSettings appSettings;
	
	public VideoFileManager(AppSettings appSettings) {
		this.appSettings = appSettings;
		// use the default folder retrieval strategy here
		this.folderRetrievalStrategy = new DefaultVideoFolderRetrievalStrategy();
	}

	public File getVideoFile(Recording recording) {
		File videoFile = recordingFileMap.get(recording);
		if (videoFile == null || !canReadAndExists(videoFile))  {
			File compressedVideoFile = getCompressedVideoFile(recording);
			File uncompressedVideoFile = getUncompressedVideoFile(recording);
			if (canReadAndExists(compressedVideoFile)) {
				recording.setRecordingStatus(RecordingStatus.COMPRESSED);
				// duration wasn't saved.. set again
				videoFile = compressedVideoFile;
				checkRecordingDuration(recording, videoFile);
			} else if (canReadAndExists(uncompressedVideoFile)) {
				recording.setRecordingStatus(RecordingStatus.UNCOMPRESSED);
				// duration wasn't saved.. set again
				videoFile = uncompressedVideoFile;
				checkRecordingDuration(recording, videoFile);
			} else if (recording.getDuration() == 0) {
				// video file does not exist and duration is set to 0, prolly nothing recorded yet
				recording.setRecordingStatus(RecordingStatus.READY);
				videoFile = uncompressedVideoFile;
			} else {
				Logger.getLogger(VideoFileManager.class).warn("No videofile found for recording with filename " + recording.getVideoFileName());
				recording.setRecordingStatus(RecordingStatus.NON_EXISTANT_FILE);
			} 
			recordingFileMap.put(recording, videoFile);
		}
		return videoFile;
	}

	/**
	 * Check if the duration for the given recording and videofile is synchronized correctly.
	 * If not, then get the duration of the video file and set it on the recording.
	 * 
	 * @param recording The recording to check the duration for
	 * @param videoFile The video file representing the given recording
	 */
	private void checkRecordingDuration(Recording recording, File videoFile) {
		if (recording.getDuration() == 0) {
			long duration = getDuration(videoFile);
			recording.setDuration(duration);
			Logger.getLogger(VideoFileManager.class).warn("Previously unsaved duration for " + recording.getVideoFileName() + " now set to " + duration);
		}
	}
	
	public void clear() {
		recordingFileMap.clear();
	}
	
	public void refreshCache(Analysis analysis) {
		for (Recording recording : analysis.getRecordings()) {
			getVideoFile(recording);
		}
	}

	public boolean canReadAndExists(File videoFile) {
		return videoFile != null && videoFile.exists() && videoFile.canRead();
	}
	
	public boolean canReadAndExists(Recording recording) {
		File videoFile = getVideoFile(recording);
		return canReadAndExists(videoFile);
	}

	public File getCompressedVideoFile(Recording recording) {
		File parentFolder = folderRetrievalStrategy.getVideoFolder(getAppSettings().getVideoDir(), recording);
		return new File(parentFolder, recording.getVideoFileName());
	}

	public File getUncompressedVideoFile(Recording recording) {
		return new File(getAppSettings().getUncompressedVideoDir(), recording.getVideoFileName());
	}
	
	public AppSettings getAppSettings() {
		return appSettings;
	}
	
	public VideoFolderRetrievalStrategy getFolderRetrievalStrategy() {
		return folderRetrievalStrategy;
	}

	public void setFolderRetrievalStrategy(VideoFolderRetrievalStrategy folderRetrievalStrategy) {
		this.folderRetrievalStrategy = folderRetrievalStrategy;
	}

	/**
	 * Get the duration of the given video file using a native video library. 
	 * Be aware that this can be a pretty expensive method invocation.
	 * 
	 * @param videoFile The file to get the duration for
	 * @return The duration
	 */
	public static long getDuration(File videoFile)  {
		long duration = 0;
		try {
			if (videoFile.exists()) {
				duration = DSJUtils.getBasicFileStats(videoFile.getAbsolutePath())[0];
			}
		} catch(DSJException e) {
			String hresultToHexString = DSJException.hresultToHexString(e.getErrorCode());
			Logger.getLogger(VideoFileManager.class).error("Failed to read meta info from for file " + 
					videoFile.getAbsolutePath() + " (hex code: 0x" + hresultToHexString + ")", e);
		}
		return duration;
	}

	public boolean hasDuplicateFiles(Recording recording) {
		return getCompressedVideoFile(recording).exists() && getUncompressedVideoFile(recording).exists();
	}

	/**
	 * Delete a video file associated with a recording. If there are both compressed and uncompressed versions, then
	 * the compressed will only be removed.
	 * 
	 * @param recording The recording to remove the videofile for
	 */
	public void deleteVideoFile(Recording recording) {
		File videoFile = getVideoFile(recording);
		if (videoFile != null && !videoFile.delete()) {
			Logger.getLogger(VideoFileManager.class).warn(videoFile.getAbsolutePath() + " could not be deleted.");
		}
	}

	public File getVideoDir() {
		return getAppSettings().getVideoDir();
	}

	public File getUncompressedVideoDir() {
		return getAppSettings().getUncompressedVideoDir();
	}
	
}
