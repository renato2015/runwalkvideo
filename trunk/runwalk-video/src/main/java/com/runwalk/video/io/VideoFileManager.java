package com.runwalk.video.io;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
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
public class VideoFileManager {

	private static final Logger LOGGER = Logger.getLogger(VideoFileManager.class);

	private Map<Recording, File> recordingFileMap = Maps.newHashMap();

	private final AppSettings appSettings;

	public VideoFileManager(AppSettings appSettings) {
		this.appSettings = appSettings;
	}
	
	private File getVideoFile(VideoFolderRetrievalStrategy videoFolderRetrievalStrategy, Recording recording) {
		File videoFile = recordingFileMap.get(recording);
		if (videoFile == null || !canReadAndExists(videoFile))  {
			File compressedVideoFile = getCompressedVideoFile(videoFolderRetrievalStrategy, recording);
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
				LOGGER.warn("No videofile found for recording with filename " + recording.getVideoFileName());
				recording.setRecordingStatus(RecordingStatus.NON_EXISTANT_FILE);
			} 
			recordingFileMap.put(recording, videoFile);
		}
		return videoFile;
	}
	
	/**
	 * Returns an {@link ImmutableSet} containing all the currently cached {@link Recording}s .
	 * Best practice is to assign this {@link Set} to a local variable directly, as calls to this method can 
	 * become expensive as the cached {@link #recordingFileMap} size grows larger.
	 * 
	 * @return The immutable set
	 */
	public Set<Recording> getCachedRecordings() {
		ImmutableSet.Builder<Recording> setBuilder = ImmutableSet.builder();
		return setBuilder.addAll(recordingFileMap.keySet()).build();
	}

	public File getVideoFile(Recording recording) {
		return getVideoFile(getVideoFolderRetrievalStrategy(), recording);
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
			LOGGER.warn("Previously unsaved duration for " + recording.getVideoFileName() + " now set to " + duration);
		}
	}

	public void refreshCache(List<Analysis> analyses) {
		recordingFileMap.clear();
		for (Analysis analysis : analyses) {
			for (Recording recording : analysis.getRecordings()) {
				getVideoFile(recording);
			}
		}
	}
	
	/**
	 * This method will iterate over the {@link Recording}s of the given {@link Analysis} and 
	 * clear it's cache entries. The method will return the number of missing video files when done refreshing.
	 * 
	 * @param analysis The analysis for which the recording cache should be refreshed
	 * @return The number of missing video files
	 */
	public int refreshCache(Analysis analysis) {
		int filesMissing = 0;
		for (Recording recording : analysis.getRecordings()) {
			File videoFile = refreshCache(recording);
			filesMissing = videoFile == null ? ++filesMissing : filesMissing;
		}
		return filesMissing;
	}
	
	public File refreshCache(Recording recording) {
		return refreshCache(getVideoFolderRetrievalStrategy(), recording);
	}
	
	public File refreshCache(VideoFolderRetrievalStrategy videoFolderRetrievalStrategy, Recording recording) {
		recordingFileMap.remove(recording);
		return getVideoFile(videoFolderRetrievalStrategy, recording);
	}

	public boolean canReadAndExists(File videoFile) {
		return videoFile != null && videoFile.exists() && videoFile.canRead();
	}

	public boolean canReadAndExists(Recording recording) {
		File videoFile = getVideoFile(recording);
		return canReadAndExists(videoFile);
	}
	
	public File getCompressedVideoFile(VideoFolderRetrievalStrategy videoFolderRetrievalStrategy, Recording recording)  {
		File parentFolder = videoFolderRetrievalStrategy.getVideoFolder(getAppSettings().getVideoDir(), recording);
		return new File(parentFolder, recording.getVideoFileName());
	}
	
	public File getCompressedVideoFile(Recording recording) {
		return getCompressedVideoFile(getVideoFolderRetrievalStrategy(), recording);
	}

	public File getUncompressedVideoFile(Recording recording) {
		return new File(getAppSettings().getUncompressedVideoDir(), recording.getVideoFileName());
	}

	public AppSettings getAppSettings() {
		return appSettings;
	}

	public VideoFolderRetrievalStrategy getVideoFolderRetrievalStrategy() {
		return getAppSettings().getVideoFolderRetrievalStrategy();
	}

	public void setVideoFolderRetrievalStrategy(VideoFolderRetrievalStrategy videoFolderRetrievalStrategy) {
		getAppSettings().setVideoFolderRetrievalStrategy(videoFolderRetrievalStrategy);
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
			if (videoFile.exists() && AppHelper.getPlatform() == PlatformType.WINDOWS) {
				duration = DSJUtils.getBasicFileStats(videoFile.getAbsolutePath())[0];
			}
		} catch(DSJException e) {
			String hresultToHexString = DSJException.hresultToHexString(e.getErrorCode());
			LOGGER.error("Failed to read meta info from for file " + 
					videoFile.getAbsolutePath() + " (hex code: 0x" + hresultToHexString + ")", e);
		}
		return duration;
	}

	public boolean hasDuplicateFiles(Recording recording) {
		return getCompressedVideoFile(recording).exists() && getUncompressedVideoFile(recording).exists();
	}

	/**
	 * Delete the video file from the disk for the given {@link Recording}. If there are both 
	 * compressed and uncompressed versions, then the only the compressed one will be removed.
	 * 
	 * @param recording The recording to remove the video file for
	 */
	private void deleteVideoFile(Recording recording) {
		File videoFile = getVideoFile(recording);
		if (videoFile != null && !videoFile.delete()) {
			LOGGER.warn(videoFile.getAbsolutePath() + " could not be deleted.");
		}
	}
	
	public void deleteVideoFiles(Analysis analysis) {
		for(Recording recording : analysis.getRecordings()) {
			deleteVideoFile(recording);
		}
	}
	
	public void deleteVideoFiles(Client client) {
		for (Analysis analysis : client.getAnalyses()) {
			deleteVideoFiles(analysis);
		}
	}

	public File getVideoDir() {
		return getAppSettings().getVideoDir();
	}

	public File getUncompressedVideoDir() {
		return getAppSettings().getUncompressedVideoDir();
	}

}
