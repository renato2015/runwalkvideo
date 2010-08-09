package com.runwalk.video.gui.media;

import java.io.File;

import org.jdesktop.application.utils.PlatformType;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSCapture.CaptureDevice;
import de.humatic.dsj.DSFilter.DSPin;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFilterInfo.DSPinInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSMediaType;

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
	
	private static final int FLAGS = DSFiltergraph.DD7;
	
	/**
	 * The selected capture device for this recorder
	 */
	private DSFilterInfo selectedDevice = null;

	private DSFilterInfo captureEncoder = VIDEO_ENCODERS[0];
	
	DSJCapturer(DSFilterInfo selectedDevice) {
		this.selectedDevice = selectedDevice;
		setFiltergraph(new DSCapture(FLAGS, selectedDevice, false, DSFilterInfo.doNotRender(), null));
		getFiltergraph().lockAspectRatio(true);
	}

	public String[] getVideoFormats() {
		String[] result = new String[] {""};
		DSPin activePin = getActivePin();
		getLogger().debug("Currently active pin : "  + activePin.getName());
		int pinIndex = activePin.getIndex();
		DSPinInfo[] downstreamPins = selectedDevice.getDownstreamPins();
		if (pinIndex < downstreamPins.length) {
			DSFilterInfo.DSPinInfo usedPinInfo = downstreamPins[pinIndex];
			DSMediaType[] mf = usedPinInfo.getFormats();
			result = new String[mf.length];
			for (int i = 0; i < mf.length; i++) {
				result[i] = mf[i].getDisplayString() + " @ " + mf[i].getFrameRate();
			}
		}
		return result;
	}

	public void setSelectedVideoFormatIndex(int index) {
		DSPin activePin = getActivePin();
		getFiltergraph().getActiveVideoDevice().setOutputFormat(activePin, index);
		getFiltergraph().getActiveVideoDevice().setOutputFormat(index);
		getLogger().debug("Pin " + getActivePin().getName() + " fps: " + getActiveDeviceFps());
	}
	
	private DSPin getActivePin() {
		CaptureDevice vDev = getFiltergraph().getActiveVideoDevice();
		DSPin previewOut = vDev.getDeviceOutput(DSCapture.CaptureDevice.PIN_CATEGORY_PREVIEW);
		DSPin captureOut = vDev.getDeviceOutput(DSCapture.CaptureDevice.PIN_CATEGORY_CAPTURE);
		return previewOut != null ? previewOut : captureOut;
	}
	
	private float getActiveDeviceFps() {
		CaptureDevice vDev = getFiltergraph().getActiveVideoDevice();
		return vDev.getFrameRate(getActivePin());
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

	public void togglePreview() {
		if (getFiltergraph().getState() == DSCapture.PREVIEW) {
			getFiltergraph().stop();
			getLogger().debug("Filtergraph for " + getTitle() + " stopped");
		} else {
			getFiltergraph().setPreview();
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
		this.captureEncoder = VIDEO_ENCODERS[index];
	}
	
	public String getSelectedCaptureEncoderName() {
		return captureEncoder.getName();
	}
	
	public String[] getCaptureEncoders() {
		String[] result = new String[VIDEO_ENCODERS.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = VIDEO_ENCODERS[i].getName();
		}
		return result;
	}

	public String getTitle() {
		return selectedDevice != null ? selectedDevice.getName() : "";
	}
	
	private boolean isRecording() {
		return getFiltergraph().getState() == DSCapture.RECORDING;
	}


}
