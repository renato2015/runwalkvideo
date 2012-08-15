package com.runwalk.video.media.dsj;

import java.util.List;

import com.google.common.collect.Lists;
import com.runwalk.video.media.IVideoCapturer;
import com.runwalk.video.settings.VideoCapturerSettings;

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
class DSJCapturer extends DSJComponent<DSCapture> implements IVideoCapturer {

	private DSFilterInfo captureEncoder;

	private String capturerName;
	
	private VideoCapturerSettings videoCapturerSettings;

	DSJCapturer(VideoCapturerSettings videoCapturerSettings) {
		this.videoCapturerSettings = videoCapturerSettings;
		this.capturerName = videoCapturerSettings.getName();
		DSFilterInfo filterInfo = DSFilterInfo.filterInfoForName(capturerName);
		setFiltergraph(new DSCapture(FLAGS, filterInfo, false, DSFilterInfo.doNotRender(), null));
		// capture encoder is resolved here
		setCaptureEncoderName(videoCapturerSettings.getEncoderName());
		// filter info for this capturer will change after intialization, if needed, get it from the active capture device
		stopRunning();
	}
	
	@Override
	public void startRunning() {
		getFiltergraph().graphChanged();
		getFiltergraph().setPreview();
		getLogger().debug("Filtergraph for " + getTitle() + " set to preview mode");
		super.startRunning();
	}

	public void startRecording(String videoPath) {
		getFiltergraph().setAviExportOptions(-1, -1, -1, getRejectPauseFilter(), -1);
		getFiltergraph().setCaptureFile(videoPath, getCaptureEncoder(), DSFilterInfo.doNotRender(), true);
		getLogger().debug("Video encoder for " + getTitle() + " set to " + getCaptureEncoder().getName());
		getLogger().debug("Pause filter rejection set to " + getRejectPauseFilter());
		getFiltergraph().record();
		showFrameDropInfo();
	}
	
	protected void showFrameDropInfo() {
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

	public boolean showCapturerSettings() {
		return getFiltergraph().getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_DEVICE) > -1;
	}

	public boolean showCameraSettings() {
		return getFiltergraph().getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_CAPTURE) > -1;
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
		videoCapturerSettings.setEncoderName(captureEncoder.getName());
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
	 * provided by {@link VideoCapturerFactory#createVideoCapturer(String, String)}.
	 */
	public String getTitle() {
		return capturerName;
	}

	protected boolean isRecording() {
		return getFiltergraph().getState() == DSCapture.RECORDING;
	}

}
