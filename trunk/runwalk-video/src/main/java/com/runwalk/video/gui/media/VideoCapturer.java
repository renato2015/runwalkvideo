package com.runwalk.video.gui.media;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.jdesktop.application.Action;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.entities.VideoFile;

public class VideoCapturer extends VideoComponent {

	public static final String CAPTURE_DEVICE = "captureDevice";

	protected static final String TIME_RECORDING = "timeRecorded";

	private static CameraDialog cameraSelectionDialog;

	private int selectedFormat = -1;

	private long timeStarted, timeRecorded;

	private IVideoCapturer capturerImpl;

	public static VideoCapturer createInstance(PropertyChangeListener listener) {
		final IVideoCapturer capturerImpl = new DSJCapturer();
		if (cameraSelectionDialog == null) {
			cameraSelectionDialog = new CameraDialog(RunwalkVideoApp.getApplication().getMainFrame(), capturerImpl);
			cameraSelectionDialog.addPropertyChangeListener(new PropertyChangeListener()  { 

				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals(CAPTURE_DEVICE)) {
						if (!evt.getNewValue().equals(evt.getOldValue())) {
							Integer selectedIndex = (Integer) evt.getNewValue();
							capturerImpl.setSelectedCaptureDeviceIndex(selectedIndex);
						}
					}
				}
			});
		} else {
			cameraSelectionDialog.setCapturerImpl(capturerImpl);
		}
		//initialize the capturer for the chosen device & encoder
		capturerImpl.startCapturer();
		return new VideoCapturer(listener, capturerImpl);
	}

	public VideoCapturer(PropertyChangeListener listener, IVideoCapturer capturerImpl) {
		super(listener);
		this.capturerImpl = capturerImpl;
		setTimer(new Timer(1000, null));
		getTimer().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				firePropertyChange(TIME_RECORDING, timeRecorded, timeRecorded = System.currentTimeMillis() - timeStarted);
				getRecording().setDuration(timeRecorded);
				//text kan je setten met een binding!!
			}
		});
		cameraSelectionDialog.setCurrentSelection(getVideoImpl().getSelectedCaptureDeviceIndex());
		cameraSelectionDialog.setLocationRelativeTo(getApplication().getMainFrame());
		getApplication().show(cameraSelectionDialog);
		setFullscreen(true);
	}

	@Action
	public void setVideoFormat() {
		String[] formats = getVideoImpl().getVideoFormats();
		String selectedFormat = (String)JOptionPane.showInputDialog(
				RunwalkVideoApp.getApplication().getMainFrame(),
				"Kies het opnameformaat:",
				"Kies opnameformaat..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				formats,
				formats[0]);
		if (selectedFormat != null) {
			this.selectedFormat = Arrays.asList(formats).indexOf(selectedFormat);
			getVideoImpl().setSelectedVideoFormatIndex(this.selectedFormat);
		}
	}
	
	@Action
	public void togglePreview() {
		getVideoImpl().togglePreview();
	}

	@Action
	public void setCaptureEncoder() {
		String[] captureEncoders = getVideoImpl().getCaptureEncoders();
		String selectedEncoder =  (String) JOptionPane.showInputDialog(
				RunwalkVideoApp.getApplication().getMainFrame(),
				"Kies een video encoder: ",
				"Video encoder wijzigen..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				captureEncoders,
				getVideoImpl().getSelectedCaptureEncoderName());
		if (selectedEncoder != null) {
			int selectedIndex = Arrays.asList(captureEncoders).indexOf(selectedEncoder);
			getVideoImpl().setSelectedCaptureEncoderIndex(selectedIndex);
			getLogger().debug("Video encoder for " + getTitle() + " changed to " + getVideoImpl().getSelectedCaptureEncoderName());
		}
	}

	public IVideoCapturer getVideoImpl() {
		return capturerImpl;
	}

	public void startRecording(Recording recording) {
		if (recording == null && !hasRecording()) {
			throw new IllegalArgumentException("No valid recording specified");
		} else if (recording != null) {
			setRecording(recording);
		}
		getRecording().setRecordingStatus(RecordingStatus.RECORDING);
		getApplication().getStatusPanel().setIndeterminate(true);
		timeStarted = System.currentTimeMillis();

		VideoFile destFile = getRecording().getUncompressedVideoFile();
		getVideoImpl().startRecording(destFile);
		getLogger().debug("Recording to file " + destFile.getAbsolutePath() + "");

		getTimer().restart();
		setState(VideoComponent.State.RECORDING);
	}

	public void stopRecording() {
		getTimer().stop();
		getVideoImpl().stopRecording();
		setState(VideoComponent.State.IDLE);
		getRecording().setRecordingStatus(RecordingStatus.UNCOMPRESSED);
	}
	
	public void showCaptureSettings() {
		getVideoImpl().showCaptureSettings();
	}

	public void showCameraSettings() {
		getVideoImpl().showCameraSettings();
	}
	
	public boolean isRecording() {
		return getState() == State.RECORDING;
	}

	@Override
	public String getTitle() {
		return getResourceMap().getString("windowTitle.text", super.getTitle());
	}
	

}
