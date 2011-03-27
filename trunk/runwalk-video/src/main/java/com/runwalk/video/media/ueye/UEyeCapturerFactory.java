package com.runwalk.video.media.ueye;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.runwalk.video.media.IVideoCapturer;
import com.runwalk.video.media.VideoCapturerFactory;
import com.runwalk.video.media.ueye.UEYE_CAMERA_LIST.ByReference;

/**
 * This factory can be used as an entry point to communicate with a uEye camera using native code.
 * 
 * @author Jeroen Peelaerts
 */
public class UEyeCapturerFactory extends VideoCapturerFactory {
	
	/**
	 * a {@link Map} that holds a mapping which associates the name of a device with it's unique ID
	 */
	private Map<String, Integer> cameraNameIdMap;

	/**
	 * {@inheritDoc}
	 */
	protected IVideoCapturer initializeCapturer(String capturerName, String captureEncoderName) {
		Integer cameraId = cameraNameIdMap.get(capturerName);
		if (cameraId != null) {
			return new UEyeCapturer(cameraId, capturerName);
		}
		throw new IllegalArgumentException("Camera " + capturerName + " is not connected to the system anymore");
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<String> getCapturerNames() {
		cameraNameIdMap = Maps.newHashMap();
		ByReference cameraNames = UEyeCapturerLibrary.GetCameraNames();
		for (int i = 0 ; i < cameraNames.dwCount; i ++) {
			UEYE_CAMERA_INFO cameraInfo = cameraNames.uci[i];
			// cleanly copy the struct's info into this map to prevent memory leaking
			cameraNameIdMap.put(cameraInfo.getModelInfo(), cameraInfo.dwCameraID);
		}
		return cameraNameIdMap.keySet();
	}

}
