package com.runwalk.video.test;

import junit.framework.TestCase;

import org.junit.Ignore;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class NativeIdsLibraryTest extends TestCase {
	
	public void testGetCameras() {
		UEYE_CAMERA_LIST.ByReference cameraNames = CuEyeCapturerLibrary.GetCameraNames();
		IntByReference cameraHandle = new IntByReference(0);
		for (int i = 0 ; i < cameraNames.dwCount; i ++) {
			UEYE_CAMERA_INFO ueye_CAMERA_INFO = cameraNames.uci[i];
			System.out.println(ueye_CAMERA_INFO.getModelInfo());
			cameraHandle.setValue(ueye_CAMERA_INFO.dwCameraID);
		}
		
		// open camera
		String settingsFile = "C:/Documents and Settings/Administrator/My Documents/uEye";
		int result = CuEyeCapturerLibrary.InitializeCamera(cameraHandle, settingsFile);
		System.out.println("initCamera result: " + result);
		System.out.println("Camera handle returned: "  + cameraHandle.getValue());
		result = CuEyeCapturerLibrary.StartRunning(cameraHandle);
		System.out.println("startRunning result: " + result);
		while(true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				CuEyeCapturerLibrary.StopRunning(cameraHandle);
			}
		}
	}
	
	@Ignore
	public void testAviTools() {
//		VideoCapturerFactory factory = VideoCapturerFactory.getInstance();
//		List<String> capturerNames = factory.getCapturerNames();
//		String last = Iterables.getLast(capturerNames);
//		
//		DSJCapturer dsjCapturer = new DSJCapturer(last) {
//			
//		};
//		
//		dsjCapturer.startRunning();
		
		
		IntByReference cameraId = new IntByReference(1);
		
		int is_InitCamera = NativeIdsCapturerLibrary.is_InitCamera(cameraId, Pointer.NULL);
		System.out.println("initCamera result: " + is_InitCamera);
		System.out.println("Camera handle returned: " + cameraId);
		IntByReference memoryId = new IntByReference(0);
		int width = 640, height = 480, bitspixel = 8, adjust = 0;
		int bufferSize = (width * ((bitspixel + 1) / 8) + adjust) * height;
		//ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
		PointerByReference buffer = new PointerByReference();
	//	int is_GetActiveImageMem = NativeIdsCapturerLibrary.is_GetActiveImageMem (cameraId.getValue(), buffer, memoryId);
	//	System.out.println("Result activeImageMem: " + is_GetActiveImageMem);
		int allocateMemory = NativeIdsCapturerLibrary.is_AllocImageMem(cameraId.getValue(), 640, 480, 8, buffer, memoryId);
//		System.out.println("allocateMemory result: " + allocateMemory);
		System.out.println("Memory handle returned: "+  buffer);
		//int loadParameters = NativeIdsCapturerLibrary.is_LoadParameters(phf, fileName);
		IntByReference aviPointer = new IntByReference(0);
		int initAvi = AviToolsLibrary.isavi_InitAVI(aviPointer, cameraId.getValue());
		System.out.println("initAvi result: " + initAvi);
		System.out.println("Avi handle returned: " + aviPointer.getValue());
		int imageSize = AviToolsLibrary.isavi_SetImageSize(aviPointer.getValue(), AviToolsLibrary.IS_AVI_CM_RGB24, width, height, 0, 0, 0);
		System.out.println("imageSize result: "+ imageSize);
		int openAvi = AviToolsLibrary.isavi_OpenAVI(aviPointer.getValue(), "C:/dump.avi");
		System.out.println("openAvi result: " + openAvi);
		int frameRate = AviToolsLibrary.isavi_SetFrameRate(aviPointer.getValue(), 25);
		System.out.println("frameRate result: "+ frameRate);
		int startAvi = AviToolsLibrary.isavi_StartAVI(aviPointer.getValue());
		System.out.println("startAvi result: "+ startAvi);
		
		//int closeAvi = AviToolsLibrary.isavi_ExitAVI(initAvi);
		//System.out.println("closeAvi result: "+ closeAvi);
	}
	
}
