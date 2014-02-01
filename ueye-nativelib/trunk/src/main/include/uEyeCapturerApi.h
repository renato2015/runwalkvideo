// ScreenCapApi.h: Defines DllScreenCap.DLL application interface
// This interface can be included by C or C++ code
//
// This is a part of the Microsoft Foundation Classes C++ library.
// Copyright (c) Microsoft Corporation.  All rights reserved.
//
// This source code is only intended as a supplement to the
// Microsoft Foundation Classes Reference and related
// electronic documentation provided with the library.
// See these sources for detailed information regarding the
// Microsoft Foundation Classes product.

#include "afxwin.h"
//#include "DynamicuEyeTools.h"

#define IS_THREAD_MESSAGE				(WM_APP + 0x0200)
#define IS_RECORDING                    0x0100
#define IS_RUNNING						0x0200
#define SET_AVI_ID						0x0300
#define SET_HWND						0x0400
/*
#ifndef UEYE_CAMERA_HANDLE_STRUCT
#define UEYE_CAMERA_HANDLE_STRUCT
typedef struct UEYE_CAMERA_HANDLE
{
  HIDS				hCam;
  UEYE_CAMERA_DATA	ucd[1];
};
#endif //UEYE_CAMERA_HANDLE_STRUCT

#ifndef UEYE_CAMERA_DATA_STRUCT
#define UEYE_CAMERA_DATA_STRUCT
typedef struct UEYE_CAMERA_DATA
{
	INT		nSizeX, nSizeY;
	INT		lMemoryId;	// grabber memory - buffer ID
	char*	pcImageMemory;// grabber memory - pointer to 
	INT*	nMonitorId;
	INT		nAviID;
	BOOL    bRecording;
	BOOL	bRunning;
	CuEyeRenderThread* pRenderThread;
};
#endif //UEYE_CAMERA_DATA_STRUCT
*/
#ifdef __cplusplus
extern "C" {
#endif  /* __cplusplus */
	INT WINAPI StartRecording(HIDS* m_hCam, const char* strFilePath, INT quality);
	INT WINAPI StopRecording(HIDS* m_hCam);
	INT WINAPI GetCameraNames(UEYE_CAMERA_LIST* cameraList);
	INT WINAPI InitializeCamera(HIDS* m_hCam);
	void WINAPI GetFrameDropInfo(HIDS* m_hCam, unsigned long* frameDropInfo);
	INT WINAPI StartRunning(HIDS* m_hCam, const wchar_t* settingsFile, LPTSTR windowName, int* monitorId, void (WINAPI*OnWindowShowCallback)(BOOL), HWND windowHandle);
	INT WINAPI StopRunning(HIDS* m_hCam);
	INT WINAPI Dispose(HIDS* m_hCam);
	void WINAPI WndToFront(HIDS* hCam);
	void WINAPI SetWndVisibility(HIDS* hCam, BOOL visible);
#ifdef __cplusplus
	}
#endif
