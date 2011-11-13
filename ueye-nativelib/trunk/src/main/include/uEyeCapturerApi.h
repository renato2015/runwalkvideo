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

#include "uEye.h"
#include "uEye_tools.h"
//#include "afxwin.h"
//#include "DynamicuEyeTools.h"

#define IS_THREAD_MESSAGE				(WM_APP + 0x0200)
#define IS_RECORDING                    0x0100
#define IS_RUNNING						0x0200
#define SET_AVI_ID						0x0300

#ifdef __cplusplus
extern "C" {
#endif  /* __cplusplus */
	INT WINAPI StartRecording(HIDS* m_hCam, const char* strFilePath);
	INT WINAPI StopRecording(HIDS* m_hCam);
	UEYE_CAMERA_LIST* WINAPI GetCameraNames();
	INT WINAPI InitializeCamera(HIDS* m_hCam);
	unsigned long* WINAPI GetFrameDropInfo(HIDS* m_hCam);
	INT WINAPI StartRunning(HIDS* m_hCam, const char* settingsFile, LPTSTR windowName, int* monitorId, void (WINAPI*OnWindowShowCallback)(BOOL), HWND windowHandle);
	INT WINAPI StopRunning(HIDS* m_hCam);
	void WINAPI WndToFront(HIDS* hCam);
	void WINAPI SetWndVisibility(HIDS* hCam, BOOL visible);
	BOOL WINAPI FilterDllMsg(LPMSG lpMsg);
	void WINAPI ProcessDllIdle();

#ifdef __cplusplus
	}
#endif
