package com.runwalk.video.test;

import junit.framework.TestCase;

import org.junit.Ignore;

import com.runwalk.video.media.VideoCapturerFactory;
import com.runwalk.video.media.ueye.UEYE_CAMERA_INFO;
import com.runwalk.video.media.ueye.UEYE_CAMERA_LIST;
import com.runwalk.video.media.ueye.UEyeAviLibrary;
import com.runwalk.video.media.ueye.UEyeCapturerLibrary;
import com.runwalk.video.media.ueye.UEyeLibrary;
import com.runwalk.video.ui.WindowManager;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

public class UEyeCapturerLibraryTest extends TestCase {
	
	public void testUEyeCapturerFactory() {
		VideoCapturerFactory.getInstance().createCapturer(null, null, null);
	}
	
	@Ignore
	public void testGetCameras() {
		UEYE_CAMERA_LIST.ByReference cameraNames = UEyeCapturerLibrary.GetCameraNames();
		final IntByReference cameraHandle = new IntByReference(0);
		char[] windowName = null;
		for (int i = 0 ; i < cameraNames.dwCount; i ++) {
			UEYE_CAMERA_INFO ueye_CAMERA_INFO = cameraNames.uci[i];
			windowName = Native.toCharArray(ueye_CAMERA_INFO.getModelInfo());
			System.out.println("WindowName returned: " + windowName);
			cameraHandle.setValue(ueye_CAMERA_INFO.dwCameraID);
		}
		cameraNames.clear();

		// open camera
		String settingsFile = "C:/Documents and Settings/Administrator/My Documents/uEye/1495LE2.ini";
		int result = UEyeCapturerLibrary.InitializeCamera(cameraHandle);
		System.out.println("initCamera result: " + result);
		System.out.println("Camera handle returned: "  + cameraHandle.getValue());
		IntByReference monitorId = new IntByReference(WindowManager.getDefaultMonitorId());
		
		UEyeCapturerLibrary.OnWndShowCallback onWndShowCallback = new UEyeCapturerLibrary.OnWndShowCallback() {
			
			public void invoke(boolean visible) {
				System.out.println("Window visibility set to " + visible);
			}
			
		};
		
		result = UEyeCapturerLibrary.StartRunning(cameraHandle, settingsFile, windowName, monitorId, onWndShowCallback);
		final IntByReference aviPointer = new IntByReference(0);
		int aviResult = UEyeCapturerLibrary.StartRecording(cameraHandle, aviPointer, "H:/test2.avi", 66.8);
		System.out.println("startRecording result: "+ aviResult);
		Thread thread = new Thread(new Runnable() {
			public void run() {
				while(true) {
					LongByReference frameDropInfo = UEyeCapturerLibrary.GetFrameDropInfo(aviPointer.getValue());
					Pointer p = frameDropInfo.getPointer();
					System.out.println("captured: " + p.getInt(0) + 
							" dropped: "+ p.getInt(1));
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						System.out.println("thread interrupted");
					}
				}
			}
		}, "FrameDropInfoThread");
		thread.start();
		try {
			// wait for the camera thread to die
			//cameraThread.join();
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		result = UEyeCapturerLibrary.StopRecording(aviPointer.getValue());
		System.out.println("stopRecording result: "+ result);
		UEyeCapturerLibrary.StopRunning(cameraHandle);
		System.out.println("Camera stopped running");
	}

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

		int is_InitCamera = UEyeLibrary.is_InitCamera(cameraId, Pointer.NULL);
		System.out.println("initCamera result: " + is_InitCamera);
		System.out.println("Camera handle returned: " + cameraId);
		IntByReference memoryId = new IntByReference(0);
		int width = 640, height = 480, bitspixel = 8, adjust = 0;
		int bufferSize = (width * ((bitspixel + 1) / 8) + adjust) * height;
		//ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
		PointerByReference buffer = new PointerByReference();
		//	int is_GetActiveImageMem = NativeIdsCapturerLibrary.is_GetActiveImageMem (cameraId.getValue(), buffer, memoryId);
		//	System.out.println("Result activeImageMem: " + is_GetActiveImageMem);
		int allocateMemory = UEyeLibrary.is_AllocImageMem(cameraId.getValue(), 640, 480, 8, buffer, memoryId);
		//		System.out.println("allocateMemory result: " + allocateMemory);
		System.out.println("Memory handle returned: "+  buffer);
		//int loadParameters = NativeIdsCapturerLibrary.is_LoadParameters(phf, fileName);
		IntByReference aviPointer = new IntByReference(0);
		int initAvi = UEyeAviLibrary.isavi_InitAVI(aviPointer, cameraId.getValue());
		System.out.println("initAvi result: " + initAvi);
		System.out.println("Avi handle returned: " + aviPointer.getValue());
		int imageSize = UEyeAviLibrary.isavi_SetImageSize(aviPointer.getValue(), UEyeAviLibrary.IS_AVI_CM_RGB24, width, height, 0, 0, 0);
		System.out.println("imageSize result: "+ imageSize);
		int openAvi = UEyeAviLibrary.isavi_OpenAVI(aviPointer.getValue(), "C:/dump.avi");
		System.out.println("openAvi result: " + openAvi);
		int frameRate = UEyeAviLibrary.isavi_SetFrameRate(aviPointer.getValue(), 25);
		System.out.println("frameRate result: "+ frameRate);
		int startAvi = UEyeAviLibrary.isavi_StartAVI(aviPointer.getValue());
		System.out.println("startAvi result: "+ startAvi);

		//int closeAvi = AviToolsLibrary.isavi_ExitAVI(initAvi);
		//System.out.println("closeAvi result: "+ closeAvi);
	}

}
