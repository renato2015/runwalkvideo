package com.runwalk.video.gui.media;

import java.io.File;
import java.util.List;

import org.jdesktop.application.utils.PlatformType;

import com.google.common.collect.Lists;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSJException;

/**
 * This class is a concrete implementation for the DirectShow for Java (dsj) libary, 
 * which enables capturing on the {@link PlatformType#WINDOWS} platform.
 * 
 * @author Jeroen Peelaerts
 */
public class DSJCapturer extends DSJComponent<DSCapture> implements IVideoCapturer {

	private DSFilterInfo captureEncoder;

	private String capturerName;

	private boolean running;
	
	DSJCapturer(String capturerName) {
		this(capturerName, null);
	}
	
	DSJCapturer(String capturerName, String captureEncoderName) {
		this.capturerName = capturerName;
		DSFilterInfo filterInfo = DSFilterInfo.filterInfoForName(capturerName);
		setFiltergraph(new DSCapture(FLAGS, filterInfo, false, DSFilterInfo.doNotRender(), null));
		// capture encoder is resolved here
		setCaptureEncoderName(captureEncoderName);
		// filter info for this capturer will change after intialization, if needed, get it from the active capture device
		stopCapturer();
	}

	/** {@inheritDoc} */
	public void startCapturer() {
		// fire a graph changed so all settings made to the filtergraph will be applied
		getFiltergraph().graphChanged();
		getFiltergraph().setPreview();
		getFiltergraph().play();
		setRunning(true);
		getLogger().debug("Filtergraph for " + getTitle() + " set to preview mode");
	}
	
	/** {@inheritDoc} */
	public void stopCapturer() {
		// stop the filtergraph so we can configure or rewire as needed
		getFiltergraph().stop();
		setRunning(false);
		getLogger().debug("Filtergraph for " + getTitle() + " stopped");
	}

	public void startRecording(File destFile) {
		getFiltergraph().setAviExportOptions(-1, -1, -1, getRejectPauseFilter(), -1);
		getFiltergraph().setCaptureFile(destFile.getAbsolutePath(), getCaptureEncoder(), DSFilterInfo.doNotRender(), true);
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
		}, "FrameDropInfoThread");
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
			stopCapturer();
		} else {
			startCapturer();
		}
	}

	public void showCaptureSettings() {
		getFiltergraph().getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_DEVICE);
	}

	public void showCameraSettings() {
		getFiltergraph().getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_CAPTURE);
	}
	
	private DSFilterInfo getCaptureEncoder() {
		return captureEncoder;
	}

	public void setCaptureEncoderName(String name) {
		if (name == null || "none".equals(name)) {
			captureEncoder = DSFilterInfo.doNotRender();
		} else {
			try {
				captureEncoder = DSFilterInfo.filterInfoForName(name);
			} catch (DSJException exc) {
				getLogger().error("Failed to resolve encoder with name " + name, exc);
				captureEncoder = DSFilterInfo.doNotRender();
			}
		}
	}

	public String getCaptureEncoderName() {
		return captureEncoder.getName();
	}

	public List<String> getCaptureEncoderNames() {
		List<String> result = Lists.newArrayList();
		DSFilterInfo[] encoders = DSEnvironment.getEncoders()[0];
		for (DSFilterInfo encoderInfo : encoders) {
			result.add(encoderInfo.getName());
		}
		return result;
	}

	/**
	 * This method should return the name of the capturer which was originally 
	 * provided by {@link VideoCapturerFactory#initializeCapturer(String, String)}.
	 */
	public String getTitle() {
		return capturerName;
	}

	private boolean isRecording() {
		return getFiltergraph().getState() == DSCapture.RECORDING;
	}


}
