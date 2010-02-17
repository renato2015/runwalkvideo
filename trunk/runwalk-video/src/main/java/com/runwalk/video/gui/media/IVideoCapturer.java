package com.runwalk.video.gui.media;

import com.runwalk.video.entities.Recording;

public interface IVideoCapturer extends IVideoComponent {

	public static final String TIME_RECORDED = "timeRecorded";

	public void startRecording(Recording recording);

	public void stopRecording();

	public void showCaptureSettings();

	public void showCameraSettings();

	public boolean isRecording();

}