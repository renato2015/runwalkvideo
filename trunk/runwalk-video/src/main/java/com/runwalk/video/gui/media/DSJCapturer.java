package com.runwalk.video.gui.media;

import java.io.File;
import java.util.List;

import org.jdesktop.application.utils.PlatformType;

import com.google.common.collect.Lists;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSCapture.CaptureDevice;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;

/**
 * This class is a concrete implementation for the DirectShow for Java (dsj) libary, 
 * which enables capturing on the {@link PlatformType#WINDOWS} platform.
 * 
 * @author Jeroen Peelaerts
 */
public class DSJCapturer extends DSJComponent<DSCapture> implements IVideoCapturer {

	public final static DSFilterInfo[] VIDEO_ENCODERS = {
		DSFilterInfo.doNotRender(), 
		DSFilterInfo.filterInfoForProfile("RunwalkVideoApp"),
		DSFilterInfo.filterInfoForName("XviD MPEG-4 Codec")
	};

	private DSFilterInfo captureEncoder = VIDEO_ENCODERS[0];

	private DSFilterInfo filterInfo;

	private String capturerName;

	private boolean running;

	DSJCapturer(String capturerName) {
		this.capturerName = capturerName;
		filterInfo = DSFilterInfo.filterInfoForName(capturerName);
		setFiltergraph(new DSCapture(FLAGS, filterInfo, false, DSFilterInfo.doNotRender(), null));
		setRunning(false);
	}

	/** {@inheritDoc} */
	public void startCapturer() {
		getFiltergraph().play();
		// clear filterInfo as it may have changed due to the filtergraph setup
		filterInfo = getFiltergraph().getActiveVideoDevice().getFilterInfo();
		getFiltergraph().lockAspectRatio(true);
		setRunning(true);
	}

	/**
	 * Return the {@link CaptureDevice} for the current {@link DSFiltergraph} if there is one.
	 * 
	 * @return The active {@link CaptureDevice}
	 */
	protected CaptureDevice getCaptureDevice() {
		return getFiltergraph() != null ? getFiltergraph().getActiveVideoDevice() : null;
	}

	private DSFilterInfo getCaptureEncoder() {
		return captureEncoder;
	}

	public void startRecording(File destFile) {
		getFiltergraph().setAviExportOptions(-1, -1, -1, getRejectPauseFilter(), -1);
		getFiltergraph().setCaptureFile(destFile.getAbsolutePath(), getCaptureEncoder(),	DSFilterInfo.doNotRender(),	true);
		getLogger().debug("Video encoder for " + getTitle() + " set to " + getCaptureEncoder().getName());
		getLogger().debug("Pause filter rejection set to " + getRejectPauseFilter());
		getFiltergraph().record();

		Thread thread = new Thread(new Runnable() {
			public void run() {
				while(isRecording()) {
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
		getFiltergraph().record();
		getFiltergraph().setPreview();
	}
	
	private boolean isRunning() {
		return running;
	}
	
	private void setRunning(boolean running) {
		this.running = running;
	}

	public void togglePreview() {
		if (isRunning()) {
			getFiltergraph().stop();
			setRunning(false);
			getLogger().debug("Filtergraph for " + getTitle() + " paused");
		} else {
			getFiltergraph().setPreview();
			getFiltergraph().graphChanged();
			getFiltergraph().play();
			setRunning(true);
			getLogger().debug("Filtergraph for " + getTitle() + " set to preview mode");
		}
	}

	public void showCaptureSettings() {
		getFiltergraph().getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_DEVICE);
	}

	public void showCameraSettings() {
		getFiltergraph().getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_CAPTURE);
	}

	public void setSelectedCaptureEncoderIndex(int index) {
		captureEncoder = VIDEO_ENCODERS[index];
	}

	public String getSelectedCaptureEncoderName() {
		return captureEncoder.getName();
	}

	public List<String> getCaptureEncoders() {
		List<String> result = Lists.newArrayList();
		for (DSFilterInfo videoEncoder : VIDEO_ENCODERS) {
			result.add(videoEncoder.getName());
		}
		return result;
	}

	/**
	 * This method should return the name of the capturer, 
	 * which was originally provided by {@link VideoCapturerFactory#initializeCapturer(String)}.
	 */
	public String getTitle() {
		return capturerName;
	}

	private boolean isRecording() {
		return getFiltergraph().getState() == DSCapture.RECORDING;
	}


}
