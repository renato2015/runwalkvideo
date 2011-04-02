package com.runwalk.video.gui.tasks;

import java.io.File;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.media.VideoCapturer;
import com.runwalk.video.io.VideoFileManager;

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
		for (VideoCapturer capturer : getCapturers()) {
			Recording recording = new Recording(getAnalysis());
			// persist recording first, then add it to the analysis
			getDaoService().getDao(Recording.class).persist(recording);
			getAnalysis().addRecording(recording);
			File videoFile = getVideoFileManager().getUncompressedVideoFile(recording);
			capturer.startRecording(recording, videoFile);
		}
		message("recordingMessage", getAnalysis().getClient().toString());
		synchronized(this) {
			while (isRecording()) {
				wait(500);
			}
		}
		boolean result = false;
		for (VideoCapturer capturer : getCapturers()) {
			capturer.stopRecording();
			File recordedFile = getVideoFileManager().getVideoFile(capturer.getRecording());
			result = result |= recordedFile != null;
			if (!result) {
				errorMessage("errorMessage", recordedFile.getAbsoluteFile());
			}
			message("endMessage", getAnalysis().getClient().toString());
		}
		return result;
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
