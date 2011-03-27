package com.runwalk.video.media.ueye;
import com.ochafik.lang.jnaerator.runtime.LibraryExtractor;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * JNA Wrapper for library <b>uEye_api.dll</b><br>
 * 
 * The functions found here define a one mapping with those exported in the <b>uEye_api.dll</b>.
 * 
 * @author Jeroen Peelaerts
 */
public class UEyeLibrary implements StdCallLibrary {
	
	public static final java.lang.String JNA_LIBRARY_NAME = LibraryExtractor.getLibraryPath("uEye_api", true, UEyeLibrary.class);
	public static final NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(UEyeLibrary.JNA_LIBRARY_NAME);
	
	static {
		Native.register(UEyeLibrary.JNA_LIBRARY_NAME);
	}
	
	public static final int IS_SUCCESS = 0;
	public static final int IS_NO_SUCCESS = -1;
	
	public static native int is_InitCamera(IntByReference phf, Pointer window);
	
	public static native int is_LoadParameters(IntByReference phf, String fileName);

	public static native int is_AllocImageMem(int cameraHandle, int width, int height, int bitspixel, PointerByReference imageMemory, IntByReference memoryId);
	
	public static native int is_SetImageMem (int cameraHandle, Pointer imageMemory, int id);	// set memory active
	
	public static native int is_GetActiveImageMem (int cameraHandle, PointerByReference buffer, IntByReference memoryId);
}
