package com.runwalk.video.media.ueye;

import com.sun.jna.Structure;
import com.sun.jna.Native;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.free.fr/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class UEyeCameraInfo extends Structure {
	/// this is the user definable camera ID
	public int dwCameraID;
	/// this is the systems enumeration ID
	public int dwDeviceID;
	/// this is the sensor ID e.g. IS_SENSOR_UI141X_M
	public int dwSensorID;
	/// flag, whether the camera is in use or not
	public boolean dwInUse;
	/**
	 * serial number of the camera<br>
	 * C type : IS_CHAR[16]
	 */
	public byte[] SerNo = new byte[16];
	/**
	 * model name of the camera<br>
	 * C type : IS_CHAR[16]
	 */
	public byte[] Model = new byte[16];
	/// various flags with camera status
	public int dwStatus;
	/// C type : DWORD[15]
	public int[] dwReserved = new int[(15)];
	
	public UEyeCameraInfo() {
		super();
	}
	/**
	 * @param dwCameraID this is the user definable camera ID<br>
	 * @param dwDeviceID this is the systems enumeration ID<br>
	 * @param dwSensorID this is the sensor ID e.g. IS_SENSOR_UI141X_M<br>
	 * @param dwInUse flag, whether the camera is in use or not<br>
	 * @param SerNo serial number of the camera<br>
	 * C type : IS_CHAR[16]<br>
	 * @param Model model name of the camera<br>
	 * C type : IS_CHAR[16]<br>
	 * @param dwStatus various flags with camera status<br>
	 * @param dwReserved C type : DWORD[15]
	 */
	public UEyeCameraInfo(int dwCameraID, int dwDeviceID, int dwSensorID, boolean dwInUse, byte[] SerNo, byte[] Model, int dwStatus, int dwReserved[]) {
		this.dwCameraID = dwCameraID;
		this.dwDeviceID = dwDeviceID;
		this.dwSensorID = dwSensorID;
		this.dwInUse = dwInUse;
		this.SerNo = SerNo;
		this.Model = Model;
		this.dwStatus = dwStatus;
		if (dwReserved.length != this.dwReserved.length) 
			throw new java.lang.IllegalArgumentException("Wrong array size !");
		this.dwReserved = dwReserved;
	}
	
	public String getModelInfo() {
		byte[] modelClone = Model.clone();
		return Native.toString(modelClone);
	}
	
	protected ByReference newByReference() { 
		return new ByReference(); 
	}
	
	protected ByValue newByValue() { 
		return new ByValue(); 
	}
	
	protected UEyeCameraInfo newInstance() { 
		return new UEyeCameraInfo(); 
	
	}
	
	/*public static UEYE_CAMERA_INFO[] newArray(int arrayLength) {
		return Structure.newArray(UEYE_CAMERA_INFO.class, arrayLength);
	}*/
	
	public static class ByReference extends UEyeCameraInfo implements Structure.ByReference {
		
	};
	public static class ByValue extends UEyeCameraInfo implements Structure.ByValue {
		
	};
}
