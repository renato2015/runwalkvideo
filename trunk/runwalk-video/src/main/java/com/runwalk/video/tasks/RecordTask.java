package com.runwalk.video.tasks;

import java.io.File;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.media.VideoCapturer;

public class RecordTask extends AbstractTask<Boolean, Void> {

	private final DaoService daoService;
	private final VideoFileManager videoFileManager;
	private final Iterable<VideoCapturer> capturers;
	private final Analysis analysis;
	private volatile boolean recording;

	public RecordTask(VideoFileManager videoFileManager, DaoService daoService, 
			Iterable<VideoCapturer> capturers, Analysis analysis) {
		super("record");
		this.daoService = daoService;
		this.videoFileManager = videoFileManager;
		this.capturers = capturers;
		this.analysis = analysis;
		this.recording = true;
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
	 * Stop recording.
	 * 
	 * @return return <code>true</code> if recording succeeded
	 */
	private Boolean stopRecording() {
		boolean result = false;
		for (VideoCapturer capturer : getCapturers()) {
			capturer.stopRecording();
			String videoPath = capturer.getVideoPath();
			Recording recording = getVideoFileManager().getRecording(videoPath);
			if ("none".equals(capturer.getCaptureEncoderName())) {
				recording.setRecordingStatus(RecordingStatus.UNCOMPRESSED);
			} else {
				recording.setRecordingStatus(RecordingStatus.COMPRESSED);
			}
			File recordedFile = getVideoFileManager().getVideoFile(recording);
			result = result |= recordedFile != null;
			if (!result) {
				errorMessage("errorMessage", recordedFile.getAbsoluteFile());
			}
			message("endMessage", getAnalysis().getClient().toString());
		}
		return result;
	}

	/**
	 * Start recording.
	 */
	private void startRecording() {
		for (VideoCapturer capturer : getCapturers()) {
			Recording recording = new Recording(getAnalysis());
			// persist recording first, then add it to the analysis
			getDaoService().getDao(Recording.class).persist(recording);
			getAnalysis().addRecording(recording);
			File videoFile = getVideoFileManager().getUncompressedVideoFile(recording);
			if (!"none".equals(capturer.getVideoImpl().getCaptureEncoderName())) {
				videoFile = getVideoFileManager().getCompressedVideoFile(recording);
			}
			File parentDir = videoFile.getParentFile();
			if (!parentDir.exists()) {
				boolean mkdirs = parentDir.mkdirs();
				getLogger().debug("Directory creation result for " + parentDir.getAbsolutePath() + " is " + mkdirs);
			}
			recording.setRecordingStatus(RecordingStatus.RECORDING);
			capturer.startRecording(videoFile);
		}
		message("recordingMessage", getAnalysis().getClient().toString());
	}
	
	public boolean isRecording() {
		return this.recording;
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

	public Analysis getAnalysis() {
		return analysis;
	}

}
