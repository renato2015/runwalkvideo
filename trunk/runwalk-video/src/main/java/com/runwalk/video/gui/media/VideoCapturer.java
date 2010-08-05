package com.runwalk.video.gui.media;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;

public class VideoCapturer extends VideoComponent {

	public static final String CAPTURE_DEVICE = "captureDevice";

	protected static final String TIME_RECORDING = "timeRecorded";

	/**
	 * Keeps track of the total number of capturer instances that have been instantiated
	 */
	private static int capturerCount = 0;
	
	private int selectedFormat = -1;

	private long timeStarted, timeRecorded;

	private IVideoCapturer capturerImpl;

	/** 
	 * This factory method creates a new {@link VideoCapturer} instance by showing a camera selection dialog. 
	 * At this time capturing is only available for {@link PlatformType#WINDOWS}. Calling this method
	 * on any other platform will return null without showing the selection dialog.
	 * 
	 * @param listener a PropertyChangeListener to notify when internal properties change
	 * @return the created instance or null if unsupported
	 * 
	 */
	public static VideoCapturer createInstance(PropertyChangeListener listener) {
		// at this moment capturing is only available on windows
		if (AppHelper.getPlatform() == PlatformType.WINDOWS) { 
			capturerCount++;
			final IVideoCapturer capturerImpl = new DSJCapturer();
			final VideoCapturer capturer = new VideoCapturer(listener, capturerImpl);
			// create a dialog to let the user choose which capture device to start on which monitor
			CameraDialog dialog = new CameraDialog(null, capturerImpl, capturer.getComponentId());
			dialog.setLocationRelativeTo(RunwalkVideoApp.getApplication().getMainFrame());
			PropertyChangeListener changeListener = new PropertyChangeListener()  { 
				
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals(CAPTURE_DEVICE)) {
						// user selected a capture device
						Integer selectedIndex = (Integer) evt.getNewValue();
						capturerImpl.setSelectedCaptureDeviceIndex(selectedIndex);
					} else if (evt.getPropertyName().equals(MONITOR_ID)) {
						// user clicked a monitor button
						int monitorId = Integer.parseInt(evt.getNewValue().toString());
						capturer.setMonitorId(monitorId);
						
					}
				}
			};
			dialog.addPropertyChangeListener(changeListener);
			//populate dialog with capture devices and look for connected monitors
			dialog.refreshCaptureDevices();
			// show the dialog on screen
			RunwalkVideoApp.getApplication().show(dialog);
			// remove the listener to avoid memory leaking
			dialog.removePropertyChangeListener(changeListener);
			// initialize the capturer's native resources for the chosen device and start running
			capturerImpl.startCapturer();
			// go fullscreen if screenId > 1, otherwise start in windowed mode on the first screen
			capturer.showComponent();
			return capturer;
		}
		Logger.getLogger(VideoCapturer.class).warn("No capturing implementation found for this platform. Capturing will be disabled.");
		return null;
	}

	private VideoCapturer(PropertyChangeListener listener, IVideoCapturer capturerImpl) {
		super(listener, capturerCount);
		this.capturerImpl = capturerImpl;
		setTimer(new Timer(1000, null));
		getTimer().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				long currentTime = System.currentTimeMillis();
				firePropertyChange(TIME_RECORDING, timeRecorded, timeRecorded = currentTime - timeStarted);
				getRecording().setDuration(timeRecorded);	
			}
		});
	}

	@Action
	public void setVideoFormat() {
		String[] formats = getVideoImpl().getVideoFormats();
		String selectedFormat = (String)JOptionPane.showInputDialog(
				null,
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
				null,
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

	public void startRecording(Recording recording, File videoFile) {
		if (videoFile == null || recording == null) {
			throw new IllegalArgumentException("No valid file or recording specified");
		} 
		setVideoFile(videoFile);
		setRecording(recording);
		
		getApplication().getStatusPanel().setIndeterminate(true);
		timeStarted = System.currentTimeMillis();
		
		getVideoImpl().startRecording(videoFile);
		recording.setRecordingStatus(RecordingStatus.RECORDING);
		getLogger().debug("Recording to file " + videoFile.getAbsolutePath() + "");

		getTimer().restart();
		setState(State.RECORDING);
	}

	public void stopRecording() {
		getTimer().stop();
		getVideoImpl().stopRecording();
		setState(State.IDLE);
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
