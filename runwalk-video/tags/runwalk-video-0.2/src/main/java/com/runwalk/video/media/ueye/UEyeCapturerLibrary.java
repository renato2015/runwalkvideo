package com.runwalk.video.media.ueye;
import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.win32.StdCallLibrary;
/**
 * JNA Wrapper for library <b>ueye-native-library</b><br>
 * This dll abstracts functionality delivered by {@link UEyeCapturerLibrary} and {@link UEyeLibrary}
 */
public class UEyeCapturerLibrary implements StdCallLibrary {

	public static final String JNA_LIBRARY_NAME = "ueye-nativelib";
	public static final NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(UEyeCapturerLibrary.JNA_LIBRARY_NAME);
	
	public static final int INIT_SUCCESS = 0;
	public static final int IS_INVALID_HANDLE = 1;
	
	static {
		Native.register(UEyeCapturerLibrary.JNA_LIBRARY_NAME);
	}

	/**
	 * Original signature : <code>int* GetCameraNames()</code><br>
	 * <i>native declaration : line 14</i>
	 */
	public static native UEYE_CAMERA_LIST.ByReference GetCameraNames();
	/**
	 * <i>native declaration : line 15</i><br>
	 * Conversion Error : LPMSG
	 */
	public static native int InitializeCamera(IntByReference cameraHandle);
	
	public static native int StartRunning(IntByReference cameraHandle, final String settingsFile, final char[] windowName, IntByReference monitorId, Callback onWndShowCallback);
	
	public static native int StopRunning(IntByReference cameraHandle);
	
	public static native int StartRecording(IntByReference cameraHandle, IntByReference aviPointer, final String path, double fps);
	
	public static native int StopRecording(int aviHandle);
	
	public static native LongByReference GetFrameDropInfo(int aviHandle);
	
	public static native void WndToFront(IntByReference cameraHandle);

	public static native void SetWndVisibility(IntByReference cameraHandle, boolean visible);
	
	/**
	 * {@link Callback} interface used for syncing the {@link UEyeCapturer}'s visibility state
	 */
	public static interface OnWndShowCallback extends StdCallCallback  {
		void invoke(boolean visible);
	}
		
}
