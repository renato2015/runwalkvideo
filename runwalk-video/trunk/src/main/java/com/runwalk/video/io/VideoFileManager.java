package com.runwalk.video.io;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.google.common.collect.ImmutableSet;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Customer;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.settings.SettingsManager;

import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSJUtils;

/**
 * This manager caches the recordings together with their corresponding video files.
 * It performs some simple file checking at data loading time to see whether the files actually exist.
 * This file checking logic was previously encapsulated in the {@link Recording} bean itself, 
 * which was a bad design choice when the idea came up to create a server component for the application, 
 * as the files are only locally accessible from each customer's file system.
 * 
 * @author Jeroen Peelaerts
 *
 */
public class VideoFileManager {

	private static final Logger LOGGER = Logger.getLogger(VideoFileManager.class);

	private Map<Recording, RecordingStatus> recordingStatusMap = new HashMap<Recording, RecordingStatus>();
	/** A sorted view on the key set of the cached {@link #recordingStatusMap}. */
	private Map<String, Recording> fileNameRecordingMap = new HashMap<String, Recording>();

	private final SettingsManager appSettings;

	public VideoFileManager(SettingsManager appSettings) {
		this.appSettings = appSettings;
	}

	/** 
	 * Get a {@link Recording} from the cache that is associated with the video file on the 
	 * given path. Will return <code>null</code> if nothing was found. 
	 * 
	 * @param path The path to get the Recording for
	 * @return The found recording
	 */
	public Recording getRecording(String videoPath) {
		Recording result = null;
		String fileName = FilenameUtils.getName(videoPath);
		// O(1) time complexity for this operation
		Recording recording = fileNameRecordingMap.get(fileName);
		if (recording.getVideoFileName().equals(fileName)) {
			result = recording;
		}
		return result;
	}
	
	public RecordingStatus getRecordingStatus(Recording recording) {
		RecordingStatus recordingStatus = recordingStatusMap.get(recording);
		return recordingStatus == null ? RecordingStatus.NON_EXISTANT_FILE : recordingStatus;
	}

	public boolean addToCache(Recording recording, RecordingStatus recordingStatus) {
		synchronized(recordingStatusMap) {
			RecordingStatus cachedRecordingStatus = recordingStatusMap.get(recording);
			if (recordingStatus != null && cachedRecordingStatus == null) {
				// O(1) time complexity for this operation
				fileNameRecordingMap.put(recording.getVideoFileName(), recording);
				// O(1) time complexity for this operation
				return recordingStatusMap.put(recording, recordingStatus) != null;
			}
		}
		return false;
	}
	
	private File resolveVideoFile(VideoFolderRetrievalStrategy videoFolderRetrievalStrategy, Recording recording, RecordingStatus recordingStatus) {
		File result = null;
		if (recordingStatus == RecordingStatus.COMPRESSED) {
			result = getCompressedVideoFile(videoFolderRetrievalStrategy, recording);
		} else if (recordingStatus == RecordingStatus.UNCOMPRESSED) {
			result = getUncompressedVideoFile(recording);
		}
		return result;
	}

	private  File getVideoFile(VideoFolderRetrievalStrategy videoFolderRetrievalStrategy, Recording recording) {
		synchronized(recordingStatusMap) {
			RecordingStatus recordingStatus = recordingStatusMap.get(recording);
			File videoFile = resolveVideoFile(videoFolderRetrievalStrategy, recording, recordingStatus);
			if (recordingStatus == null)  {
				File compressedVideoFile = getCompressedVideoFile(videoFolderRetrievalStrategy, recording);
				File uncompressedVideoFile = getUncompressedVideoFile(recording);
				if (canReadAndExists(compressedVideoFile)) {
					recordingStatus = RecordingStatus.COMPRESSED;
					videoFile = compressedVideoFile;
					// duration wasn't saved.. set again
					checkRecordingDuration(recording, videoFile);
				} else if (canReadAndExists(uncompressedVideoFile)) {
					recordingStatus = RecordingStatus.UNCOMPRESSED;
					// duration wasn't saved.. set again
					videoFile = uncompressedVideoFile;
					checkRecordingDuration(recording, videoFile);
				} else if (recording.getDuration() == 0) {
					// video file does not exist and duration is set to 0, prolly nothing recorded yet
					recordingStatus = RecordingStatus.READY;
					videoFile = uncompressedVideoFile;
				} else {
					LOGGER.warn("No videofile found for recording with filename " + recording.getVideoFileName());
					recordingStatus = RecordingStatus.NON_EXISTANT_FILE;
				} 
				addToCache(recording, recordingStatus);
			}
			return videoFile;
		}
	}
	
	public boolean isRecorded(List<Recording> recordings) {
		boolean result = false;
		for (Recording recording : recordings) {
			RecordingStatus recordingStatus = recordingStatusMap.get(recording);
			result |= recordingStatus != null && recordingStatus.isRecorded();
		}
		return result;
	}
	
	public boolean isRecorded(Recording... recordings) {
		return isRecorded(Arrays.asList(recordings));
	}

	/**
	 * Returns an {@link ImmutableSet} containing all the currently cached {@link Recording}s .
	 * Best practice is to assign this {@link Set} to a local variable directly, as calls to this method can 
	 * become expensive as the cached {@link #recordingStatusMap} size grows larger.
	 * 
	 * @return The immutable set
	 */
	public Set<Recording> getCachedRecordings() {
		ImmutableSet.Builder<Recording> setBuilder = ImmutableSet.builder();
		return setBuilder.addAll(recordingStatusMap.keySet()).build();
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
	
	public boolean updateCache(Recording recording, RecordingStatus recordingStatus) {
		// O(1) time complexity for this operation
		fileNameRecordingMap.put(recording.getVideoFileName(), recording);
		// O(1) time complexity for this operation
		return recordingStatusMap.put(recording, recordingStatus) != null;
	}
	
	public int refreshCache() {
		int filesMissing = 0;
		for (Recording recording : recordingStatusMap.keySet()) {
			filesMissing += refreshCache(recording);
		}
		return filesMissing;
	}

	public int refreshCache(List<Analysis> analyses) {
		int filesMissing = 0;
		for (Analysis analysis : analyses) {
			filesMissing += refreshCache(analysis);
		}
		return filesMissing;
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
			filesMissing += refreshCache(recording);
		}
		return filesMissing;
	}

	public int refreshCache(Recording recording) {
		File videoFile = refreshCache(getVideoFolderRetrievalStrategy(), recording);
		return videoFile == null ? 1 : 0;
	}
	
	public File refreshCache(VideoFolderRetrievalStrategy videoFolderRetrievalStrategy, Recording recording) {
		removeRecording(recording);
		return getVideoFile(videoFolderRetrievalStrategy, recording);
	}
	
	private boolean removeRecording(Recording recording) {
		RecordingStatus removedRecordingStatus = recordingStatusMap.remove(recording);
		Recording removedRecording = fileNameRecordingMap.remove(recording.getVideoFileName());
		return removedRecordingStatus != null && removedRecording != null;
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

	public SettingsManager getAppSettings() {
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
		removeRecording(recording);
		if (videoFile != null) {
			videoFile.deleteOnExit();
			LOGGER.debug(videoFile.getAbsolutePath() + " scheduled for removal.");
		}
	}

	public void deleteVideoFiles(Analysis analysis) {
		for(Recording recording : analysis.getRecordings()) {
			deleteVideoFile(recording);
		}
	}

	public void deleteVideoFiles(Customer customer) {
		for (Analysis analysis : customer.getAnalyses()) {
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
