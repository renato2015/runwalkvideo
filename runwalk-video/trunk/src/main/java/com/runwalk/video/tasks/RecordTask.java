package com.runwalk.video.tasks;

import java.io.File;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.media.VideoCapturer;
import com.runwalk.video.model.AnalysisModel;

public class RecordTask extends AbstractTask<Boolean, Void> {

	private final DaoService daoService;
	private final VideoFileManager videoFileManager;
	private final Iterable<VideoCapturer> capturers;
	private final AnalysisModel analysisModel;
	private volatile boolean recording = false;

	public RecordTask(VideoFileManager videoFileManager, DaoService daoService, 
			Iterable<VideoCapturer> capturers, AnalysisModel analysisModel) {
		super("record");
		this.daoService = daoService;
		this.videoFileManager = videoFileManager;
		this.capturers = capturers;
		this.analysisModel = analysisModel;
	}

	protected Boolean doInBackground() throws Exception {
		message("startMessage");
		startRecording();
		synchronized(this) {
			while (isRecording()) {
				wait(500);
			}
		}
		return stopRecording();
	}
	
	/**
	 * Start recording.
	 */
	private void startRecording() {
		for (VideoCapturer capturer : getCapturers()) {
			Recording recording = new Recording(getAnalysisModel().getEntity());
			// persist recording first, then add it to the analysis
			getDaoService().getDao(Recording.class).persist(recording);
			getAnalysisModel().addRecording(recording);
			File videoFile = getVideoFileManager().getUncompressedVideoFile(recording);
			if (!"none".equals(capturer.getVideoImpl().getCaptureEncoderName())) {
				videoFile = getVideoFileManager().getCompressedVideoFile(recording);
			}
			getVideoFileManager().addToCache(recording, videoFile);
			File parentDir = videoFile.getParentFile();
			if (!parentDir.exists()) {
				boolean mkdirs = parentDir.mkdirs();
				getLogger().debug("Directory creation result for " + parentDir.getAbsolutePath() + " is " + mkdirs);
			}
			capturer.startRecording(videoFile.getAbsolutePath());
			// set recording to true if recording / file key value pair added to file manager
			setRecording(true);
		}
		message("recordingMessage", getAnalysisModel().toString());
	}

	/**
	 * Stop recording.
	 * 
	 * @return return <code>true</code> if recording succeeded
	 */
	private Boolean stopRecording() {
		boolean result = true;
		for (VideoCapturer capturer : getCapturers()) {
			capturer.stopRecording();
			for (Recording recording : getAnalysisModel().getEntity().getRecordings()) {
				String videoPath = capturer.getVideoPath();
				File videoFile = getVideoFileManager().getVideoFile(recording);
				result &= videoFile != null;
				if (videoFile != null && videoPath != null && videoPath.equals(videoFile.getAbsolutePath())) {
					if ("none".equals(capturer.getCaptureEncoderName())) {
						recording.setStatusCode(RecordingStatus.UNCOMPRESSED.getCode());
					} else {
						recording.setStatusCode(RecordingStatus.COMPRESSED.getCode());
					}
				} else {
					errorMessage("errorMessage", recording);
				}
			}
		}
		message("endMessage", getAnalysisModel().toString());
		return result;
	}

	public boolean isRecording() {
		return recording;
	}
	
	public void setRecording(boolean recording) {
		this.recording = recording;
	}

	public DaoService getDaoService() {
		return daoService;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public Iterable<VideoCapturer> getCapturers() {
		return capturers;
	}

	public AnalysisModel getAnalysisModel() {
		return analysisModel;
	}

}
