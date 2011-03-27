package com.runwalk.video.media.ueye;

import com.ochafik.lang.jnaerator.runtime.LibraryExtractor;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.ptr.IntByReference;

/**
 * JNA Wrapper for library <b>uEye_tools.dll</b><br>
 * 
 * The functions found here define a one mapping with those exported in the <b>uEye_tools.dll</b>.
 * 
 * @author Jeroen Peelaerts
 */
public class UEyeAviLibrary implements Library {
	
	public static final java.lang.String JNA_LIBRARY_NAME = LibraryExtractor.getLibraryPath("uEye_tools", true, UEyeAviLibrary.class);
	public static final NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(UEyeAviLibrary.JNA_LIBRARY_NAME, com.ochafik.lang.jnaerator.runtime.MangledFunctionMapper.DEFAULT_OPTIONS);

	public static final int IS_AVI_CM_BAYER = 11;
	public static final int IS_AVI_CM_RGB24 = 1;
	public static final int IS_AVI_CM_Y8 = 6;
	public static final int IS_AVI_CM_RGB32 = 0;
	public static final int IS_AVI_NO_ERR = 0;
	public static final int IS_AVI_SET_EVENT_FRAME_SAVED = 1;
	public static final int ISAVIERRBASE = 300;
	
	static {
		Native.register(UEyeAviLibrary.JNA_LIBRARY_NAME);
	}

	public static native int isavi_InitAVI(IntByReference pnAviID, int cameraHandle);
	
	public static native int isavi_OpenAVI(int nAviID, String strFileName);
	
	public static native int isavi_StartAVI(int nAviID);
	
	public static native int isavi_StopAVI(int nAviID);
	
	public static native int isavi_ExitAVI(int nAviID);
	
	public static native int isavi_SetImageSize(int nAviID, int cMode, int Width, int Height, int PosX, int PosY, int LineOffset);
	
	/*! \brief	Sets the frame rate of the video. The frame rate can be changed at any time if the avi file is 
	*			already created.
	*
	*
	*  \param   nAviID:			Instance ID returned by isavi_InitAVI()
	*  \param	pcImageMem:		Pointer to data image
	*
	*  \return	IS_AVI_NO_ERR			No error, initialisation was successful
	*  \return	IS_AVI_ERR_INVALID_ID	The specified instance could not be found. The ID is either invalid or the 
	*									specified interface has already been destroyed by a previous call to 
	*									isavi_ExitAVI().
	*  \return	IS_AVI_ERR_WRITE_INFO	The AVI file could not be modified
	***********************************************************************************************************/
	public static native int isavi_SetFrameRate(int nAviID,double fr);
	
	/*! \brief	Sets the quality of the actual image that is going to be compressed and added to the video stream. 
	*			The quality can be changed at any time. The best image quality is 100 (bigger avi file size) and 
	*			the worst is 1.
	*
	*
	*  \param   nAviID:	Instance ID returned by isavi_InitAVI()
	*  \param	q:		Quality of compression [1…100] 
	*
	*  \return	IS_AVI_NO_ERR				No error, initialisation was successful
	*  \return	IS_AVI_ERR_INVALID_ID		The specified instance could not be found. The ID is either invalid 
	*										for the specified interface has already been destroyed by a previous 
	*										call to isavi_ExitAVI().
	*  \return	IS_AVI_ERR_INVALID_VALUE	The parameter q is bigger than 100 or less than 1
	***********************************************************************************************************/
	public static native int isavi_SetImageQuality(int nAviID, int q);
	
	
}
