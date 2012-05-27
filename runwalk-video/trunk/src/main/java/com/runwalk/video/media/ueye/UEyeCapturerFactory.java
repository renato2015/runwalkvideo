package com.runwalk.video.media.ueye;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdesktop.application.utils.PlatformType;

import com.google.common.collect.Maps;
import com.runwalk.video.media.IVideoCapturer;
import com.runwalk.video.media.VideoCapturerFactory;

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
	
	public UEyeCapturerFactory() { 	}

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
	 * This method will retrieve a {@link Collection} containing the names of
	 * the two first connected {@link UEyeCapturer}s.
	 * 
	 * @return A collection containg the retrieved camera names
	 */
	public Collection<String> getVideoCapturerNames() {
		cameraNameIdMap = Maps.newHashMap();
		// create native struct to retrieve 2 camera information from the library
		UEyeCameraList.ByValue uEyeCameraList = new UEyeCameraList.ByValue();
		// write struct to native memory
		uEyeCameraList.write();
		// get camera information by passing the pointer to the created struct
		int result = UEyeCapturerLibrary.GetCameraNames(uEyeCameraList.getPointer());
		String isSuccess = result == 0 ? "succeeded" : "failed (" + result + ")";
		Logger.getLogger(getClass()).debug("GetCameraNames " + isSuccess);
		// read struct information back into java
		uEyeCameraList.read();
		// enumerate available camera's
		for (int i = 0 ; i < uEyeCameraList.dwCount; i ++) {
			UEyeCameraInfo cameraInfo = uEyeCameraList.uci[i];
			if (!cameraInfo.dwInUse) {
				// cleanly copy the struct's info into this map to prevent memory leaking
				cameraNameIdMap.put(cameraInfo.getModelInfo(), cameraInfo.dwCameraID);
			}
		}
		return cameraNameIdMap.keySet();
	}

	protected boolean isPlatformSupported(PlatformType platformType) {
		return platformType == PlatformType.WINDOWS;
	}

}
