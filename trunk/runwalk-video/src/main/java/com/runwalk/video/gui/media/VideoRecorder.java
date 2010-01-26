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
import com.runwalk.video.util.AppSettings;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSMediaType;
import de.humatic.dsj.DSCapture.CaptureDevice;
import de.humatic.dsj.DSFilter.DSPin;

public class VideoRecorder extends VideoComponent<DSCapture> {

	public static final String TIME_RECORDED = "timeRecorded";

	public static final String CAPTURE_DEVICE = "captureDevice";

	private static final DSFilterInfo[] VIDEO_ENCODERS = AppSettings.VIDEO_ENCODERS;

	private static DSFilterInfo[][] dsi;

	private CameraDialog cameraSelectionDialog;
	
	/**
	 * The selected capture device for this recorder
	 */
	private DSFilterInfo selectedDevice = null;

	private int selectedFormat = -1;

	private long timeStarted, timeRecorded;

	private DSFilterInfo captureEncoder = VIDEO_ENCODERS[0];

	public static String[] queryCaptureDevices() {
		dsi = DSCapture.queryDevices(1);
		String[] devices = new String[dsi[0].length];
		for (int i = 0; i < dsi[0].length; i++) {
			devices[i] = dsi[0][i].getName();
		}
		return devices;
	}
	
	public VideoRecorder(PropertyChangeListener listener) {
		super(listener);
		initFiltergraph();
	}
	
	@Action
	public void setVideoFormat() {
		CaptureDevice vDev = getFiltergraph().getActiveVideoDevice();
		DSPin previewOut = vDev.getDeviceOutput(DSCapture.CaptureDevice.PIN_CATEGORY_PREVIEW);
		DSPin captureOut = vDev.getDeviceOutput(DSCapture.CaptureDevice.PIN_CATEGORY_CAPTURE);
		DSPin activeOut = previewOut != null ? previewOut : captureOut;
		int pinIndex = activeOut.getIndex();
		getLogger().debug("Currently active pin : "  + activeOut.getName());
		DSFilterInfo.DSPinInfo usedPinInfo = selectedDevice.getDownstreamPins()[pinIndex];
		DSMediaType[] mf = usedPinInfo.getFormats();
		Object[] formats = new String[mf.length];

		for (int i = 0; i < mf.length; i++) {
			formats[i] = mf[i].getDisplayString() + " @ " + mf[i].getFrameRate();
		}
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
			getFiltergraph().getActiveVideoDevice().setOutputFormat(activeOut, this.selectedFormat);
			getFiltergraph().getActiveVideoDevice().setOutputFormat(this.selectedFormat);
			getLogger().debug((previewOut != null ? "preview" : "capture")+" fps: "+vDev.getFrameRate(activeOut));
		}
	}
	
	private DSFilterInfo getCaptureEncoder() {
		return captureEncoder;
	}
	
	@Action
	public void setCaptureEncoder() {
		if (getFiltergraph() != null) {
			String[] filterInfo = new String[VIDEO_ENCODERS.length];
			int i = 0;
			for (DSFilterInfo fInfo : VIDEO_ENCODERS) {
				filterInfo[i] = fInfo.getName();
				i++;
			}
			String selectedEncoder =  (String) JOptionPane.showInputDialog(
					RunwalkVideoApp.getApplication().getMainFrame(),
					"Kies een video encoder: ",
					"Video encoder wijzigen..",
					JOptionPane.PLAIN_MESSAGE,
					null,
					filterInfo,
					this.captureEncoder.getName());
			if (selectedEncoder != null) {
				int selectedIndex = Arrays.asList(filterInfo).indexOf(selectedEncoder);
				this.captureEncoder = VIDEO_ENCODERS[selectedIndex];
				getLogger().debug("Video encoder for " + getName() + " changed to " + this.captureEncoder.getName());
			}
		}
	}

	public DSCapture initFiltergraph() {
		if (cameraSelectionDialog == null) {
			cameraSelectionDialog = new CameraDialog(getApplication().getMainFrame());
			cameraSelectionDialog.addPropertyChangeListener(new PropertyChangeListener()  { 

				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals(CAPTURE_DEVICE)) {
						if (!evt.getNewValue().equals(evt.getOldValue())) {
							Integer selectedIndex = (Integer) evt.getNewValue();
							setSelectedCaptureDeviceIndex(selectedIndex);
						}
					}
				}
			});
		}
		setTimer(new Timer(1000, null));
		getTimer().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				firePropertyChange(TIME_RECORDED, timeRecorded, timeRecorded = System.currentTimeMillis() - timeStarted);
				getRecording().setDuration(timeRecorded);
				//text kan je setten met een binding!!
			}
		});
		cameraSelectionDialog.setCurrentSelection(getSelectedCaptureDeviceIndex());
		cameraSelectionDialog.getComponent().setLocationRelativeTo(getApplication().getMainFrame());
		getApplication().show(cameraSelectionDialog.getComponent());
		setFiltergraph(new DSCapture(DSFiltergraph.D3D9, selectedDevice, false, DSFilterInfo.doNotRender(), getPropertyChangeListeners()[0]));
		getFiltergraph().lockAspectRatio(true);
		return getFiltergraph();
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

		getFiltergraph().setAviExportOptions(-1, -1, -1, getRejectPauseFilter(), -1);
		getFiltergraph().setCaptureFile(destFile.getAbsolutePath(), getCaptureEncoder(),	DSFilterInfo.doNotRender(),	true);
		getLogger().debug("Movie recording to file " + destFile.getAbsolutePath() + "");
		getLogger().debug("Video encoder = " + getCaptureEncoder().getName() + ".");
		getLogger().debug("Pause filter rejection set to " + getRejectPauseFilter()+ ".");

		getFiltergraph().record();
		getTimer().restart();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				while(getFiltergraph().getState() == DSCapture.RECORDING) {
					getLogger().debug("captured: " + getFiltergraph().getFrameDropInfo()[0] + 
							" dropped: "+ getFiltergraph().getFrameDropInfo()[1]);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						getLogger().error(e);
					}
				}
			}
		});
		thread.start();
	}
	
	public void stopRecording() {
		getTimer().stop();
		getFiltergraph().record();
		getFiltergraph().setPreview();
		getRecording().setRecordingStatus(RecordingStatus.UNCOMPRESSED);
	}

	@Action
	public void togglePreview() {
		if (getFiltergraph().getState() == DSCapture.PREVIEW) {
			getFiltergraph().stop();
		} else {
			getFiltergraph().setPreview();
		}
	}
	
	public void showCaptureSettings() {
		getFiltergraph().getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_DEVICE);
	}

	public void showCameraSettings() {
		getFiltergraph().getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_CAPTURE);
	}

	private void setSelectedCaptureDeviceIndex(int selectedIndex) {
		this.selectedDevice = dsi[0][selectedIndex];
	}

	private int getSelectedCaptureDeviceIndex() {
		return Arrays.asList(dsi[0]).indexOf(selectedDevice);
	}

	public String getName() {
		return "Camera: " + selectedDevice.getName();
	}

	public boolean isRecording() {
		return getFiltergraph().getState() == DSCapture.RECORDING;
	}

}
