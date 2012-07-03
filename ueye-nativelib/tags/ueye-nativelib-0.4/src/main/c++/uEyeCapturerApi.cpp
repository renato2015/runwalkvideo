// File: Exports.cpp
// Copyright (c) Microsoft Corporation.  All rights reserved.
//
// This source code is only intended as a supplement to the
// Microsoft Classes Reference and related electronic
// documentation provided with the library.
// See these sources for detailed information regarding the
// Microsoft C++ Libraries products.

#include "stdafx.h"
// include for visual leak detector (only for debugging)
// #include "vld.h"
#include "uEyeCapturerApi.h"
#include "uEyeCapturer.h"
#include "uEyeRenderThread.h"

INT		m_nBitsPerPixel;// number of bits needed store one pixel
INT		m_nSizeX, m_nSizeY;
INT		m_lMemoryId;	// grabber memory - buffer ID
char*	m_pcImageMemory;// grabber memory - pointer to 
INT*	m_monitorId;
INT		m_nAviID = 0;
BOOL    m_bRecording = FALSE;
BOOL	m_bRunning = FALSE;
CuEyeRenderThread* m_renderThread;

/* Image size functions */
INT GetImagePos(HIDS m_hCam, INT &xPos, INT &yPos)
{ 
	IS_RECT rectAOI;
	INT nRet = is_AOI(m_hCam, IS_AOI_IMAGE_GET_AOI, (void*)&rectAOI, sizeof(rectAOI));
	if (nRet == IS_SUCCESS) {
		xPos = rectAOI.s32X;
		yPos = rectAOI.s32Y;
	}
	return IS_SUCCESS; 
}

INT GetImageSize(HIDS m_hCam, INT &xSze, INT &ySze)
{
	IS_RECT rectAOI;
	INT nRet = is_AOI(m_hCam, IS_AOI_IMAGE_GET_AOI, (void*)&rectAOI, sizeof(rectAOI));
	if (nRet == IS_SUCCESS) {
		xSze = rectAOI.s32Width;
		ySze = rectAOI.s32Height;
	}
	return IS_SUCCESS;
}

void GetMaxImageSize(HIDS* m_hCam, INT *pnSizeX, INT *pnSizeY)
{
    // Check if the camera supports an arbitrary AOI
    INT nAOISupported = 0;
    BOOL bAOISupported = TRUE;
    if (is_ImageFormat(*m_hCam,
                       IMGFRMT_CMD_GET_ARBITRARY_AOI_SUPPORTED, 
                       (void*)&nAOISupported, 
                       sizeof(nAOISupported)) == IS_SUCCESS) {
        bAOISupported = (nAOISupported != 0);
    }

    if (bAOISupported) {
        // Get maximum image size
	    SENSORINFO sInfo;
	    is_GetSensorInfo (*m_hCam, &sInfo);
	    *pnSizeX = sInfo.nMaxWidth;
	    *pnSizeY = sInfo.nMaxHeight;
    }
    else
    {
		GetImageSize(*m_hCam, *pnSizeX, *pnSizeY);
    }
}

INT GetColorMode(HIDS m_hCam) {
	if ( !m_hCam ) {
		return IS_NO_SUCCESS;
	}
	return is_SetColorMode( m_hCam, IS_GET_COLOR_MODE );  
}

INT GetFrameRate( HIDS hCam, double &fr) { 
	return is_SetFrameRate( hCam, IS_GET_FRAMERATE, &fr); 
}

INT WINAPI InitializeCamera(HIDS* m_hCam) {
	// open camera with the given ID
	INT result = is_InitCamera (m_hCam, NULL);
	if (result == IS_STARTER_FW_UPLOAD_NEEDED) {
        // Time for the firmware upload = 25 seconds by default
        INT nUploadTime = 25000;
        is_GetDuration (*m_hCam, IS_STARTER_FW_UPLOAD, &nUploadTime);
        /*CString Str1, Str2, Str3;
        Str1 = "This camera requires a new firmware. The upload will take about";
        Str2 = "seconds. Please wait ...";
        Str3.Format ("%s %d %s", Str1, nUploadTime / 1000, Str2);
        AfxMessageBox (Str3, MB_ICONWARNING);*/
        // Try again to open the camera. This time we allow the automatic upload of the firmware by
        // specifying "IS_ALLOW_STARTER_FIRMWARE_UPLOAD"
        *m_hCam = (HIDS) (((INT)*m_hCam) | IS_ALLOW_STARTER_FW_UPLOAD); 
        result = is_InitCamera (m_hCam, NULL);
    }	
	return result;
}

INT LoadSettings(HIDS* m_hCam, const wchar_t* settingsFile) {
	INT result = IS_SUCCESS;
	if (settingsFile != NULL && *settingsFile != '\0') {
		TRACE("Loading settings from %s\n", settingsFile);
		result = is_ParameterSet(*m_hCam, IS_PARAMETERSET_CMD_LOAD_FILE, (void*) settingsFile, NULL);
		if (result == IS_SUCCESS) {
			// realloc image mem with actual sizes and depth.
			is_FreeImageMem( *m_hCam, m_pcImageMemory, m_lMemoryId );
			GetImageSize( *m_hCam, m_nSizeX, m_nSizeY); 
			switch( is_SetColorMode( *m_hCam, IS_GET_COLOR_MODE ) )
			{
			case IS_SET_CM_RGB32:
				m_nBitsPerPixel = 32;
				break;
			case IS_SET_CM_RGB24:
				m_nBitsPerPixel = 24;
				break;
			case IS_SET_CM_RGB16:
			case IS_SET_CM_UYVY:
				m_nBitsPerPixel = 16;
				break;
			case IS_SET_CM_RGB15:
				m_nBitsPerPixel = 15;
				break;
			case IS_SET_CM_Y8:
			case IS_SET_CM_RGB8:
			case IS_SET_CM_BAYER:
			default:
				m_nBitsPerPixel = 8;
				break;
			}
		}
	}
	return result;
}

INT WINAPI StopRunning(HIDS* m_hCam) {
	INT result = IS_SUCCESS;
	AFX_MANAGE_STATE(AfxGetStaticModuleState());
	if( m_bRunning && !m_bRecording) {
		// stop video event notification
		result = is_StopLiveVideo( *m_hCam, IS_WAIT );
		m_bRunning = FALSE;
	}
	return result;
}

INT WINAPI Dispose(HIDS* m_hCam) {
	INT result = IS_SUCCESS;
	AFX_MANAGE_STATE(AfxGetStaticModuleState());
	if( m_bRunning && !m_bRecording) {
		// stop rendering thread
		PostThreadMessage(m_renderThread->m_nThreadID, IS_THREAD_MESSAGE, IS_RUNNING, FALSE);
		// wait for thread to terminate
		WaitForSingleObject(m_renderThread->m_hThread, INFINITE);
		// stop video event notification
		result = is_StopLiveVideo( *m_hCam, IS_WAIT );
		// run rendering thread destructor
		delete m_renderThread;
		m_renderThread = NULL;
		// close AVI handle and reset avi instance ID
		if (m_nAviID) {
			isavi_ExitAVI(m_nAviID);
			m_nAviID = 0;
		}
		// Free the allocated buffer
		if( m_pcImageMemory != NULL ) {
			is_FreeImageMem( *m_hCam, m_pcImageMemory, m_lMemoryId );
		}	
		m_pcImageMemory = NULL;
		// Close camera
		result |= is_ExitCamera(*m_hCam );
		m_hCam = NULL;
		m_bRunning = FALSE;
	} else if (m_hCam) {
		// Close camera
		result = is_ExitCamera(*m_hCam );
		m_hCam = NULL;
	}
	return result;
}

///////////////////////////////////////////////////////////////////////////////
//
// METHOD CIdsSimpleLiveDlg::InitDisplayMode() 
//
// DESCRIPTION: - initializes the display mode
//
///////////////////////////////////////////////////////////////////////////////
int InitDisplayMode(HIDS* m_hCam)
{
    INT result = IS_SUCCESS;
    INT		nColorMode;	// Y8/RGB16/RGB24/REG32
    if (m_hCam == NULL) {
		return IS_NO_SUCCESS;
	}
	
	// Get sensor info
	SENSORINFO m_sInfo;			// sensor information struct
	is_GetSensorInfo(*m_hCam, &m_sInfo);

    if (m_pcImageMemory != NULL) {
        is_FreeImageMem( *m_hCam, m_pcImageMemory, m_lMemoryId );
    }
    m_pcImageMemory = NULL;

	// Set display mode to DIB
    result = is_SetDisplayMode(*m_hCam, IS_SET_DM_DIB);
	if (m_sInfo.nColorMode == IS_COLORMODE_BAYER) {
		// setup the color depth to the current windows setting
        is_GetColorDepth(*m_hCam, &m_nBitsPerPixel, &nColorMode);
    } else if (m_sInfo.nColorMode == IS_COLORMODE_CBYCRY) {
        // for color camera models use RGB32 mode
        nColorMode = IS_SET_CM_RGB32;
        m_nBitsPerPixel = 32;
    } else {
        // for monochrome camera models use Y8 mode
        nColorMode = IS_SET_CM_Y8;
        m_nBitsPerPixel = 8;
    }

    // allocate an image memory.
    if (is_AllocImageMem(*m_hCam, m_nSizeX, m_nSizeY, m_nBitsPerPixel, &m_pcImageMemory, &m_lMemoryId ) != IS_SUCCESS) {
        AfxMessageBox(TEXT("Memory allocation failed!"), MB_ICONWARNING );
    } else {
		is_SetImageMem( *m_hCam, m_pcImageMemory, m_lMemoryId );
	}

    if (result == IS_SUCCESS) {
        // set the desired color mode
        result = is_SetColorMode(*m_hCam, nColorMode);
		// Sets the position and size of the image by using an object of the IS_RECT type.
		IS_RECT rectAOI;
		rectAOI.s32X     = 0;
		rectAOI.s32Y     = 0;
		rectAOI.s32Width = m_nSizeX;
		rectAOI.s32Height = m_nSizeY;
		result |= is_AOI(*m_hCam, IS_AOI_IMAGE_SET_AOI, (void*)&rectAOI, sizeof(rectAOI));
    }   
    return result;
}

INT WINAPI StartRunning(HIDS* m_hCam, const wchar_t* settingsFile, int* nMonitorId, void (WINAPI*OnWindowShow)(BOOL), HWND hWnd) { 
	AFX_MANAGE_STATE(AfxGetStaticModuleState());
	m_monitorId = nMonitorId;
	INT result = IS_SUCCESS;
	if (!m_bRunning && m_hCam) {
		if (!m_renderThread) {
			if( LoadSettings(m_hCam, settingsFile) != IS_SUCCESS) {
				// if settings from file cannot be loaded, then use default maximum size for the sensor
				GetMaxImageSize(m_hCam, &m_nSizeX, &m_nSizeY);
			}
			result = InitDisplayMode(m_hCam);
			if (result == IS_SUCCESS && !m_renderThread) {
				// start a new thread that will create a window to show the live video fullscreen
				m_renderThread = (CuEyeRenderThread *) AfxBeginThread(RUNTIME_CLASS(CuEyeRenderThread), THREAD_PRIORITY_NORMAL, 0, CREATE_SUSPENDED);
				// set to false to prevent uncontrolled m_renderThread pointer invalidation
				m_renderThread->m_bAutoDelete = FALSE; 
				m_renderThread->Initialize(m_hCam, m_pcImageMemory, &m_lMemoryId, nMonitorId, OnWindowShow, hWnd);
				m_renderThread->ResumeThread();
			}
		} else if (!m_renderThread->m_pMainWnd && m_renderThread->GetHwnd() != hWnd) {
			// stop running was called and window handle invalidated
			PostThreadMessage(m_renderThread->m_nThreadID, IS_THREAD_MESSAGE, SET_HWND, (LPARAM) hWnd);
		}
		// start live video
		result |= is_CaptureVideo( *m_hCam, IS_DONT_WAIT );
		m_bRunning = TRUE;
	}
	return result;
}

/*
* Camera handle added here as unused argument, considering future multi ueye support
*/
void WINAPI WndToFront(HIDS* hCam) 
{
	if (m_renderThread) {
		// TODO should get the thread associated with the given camera handle here..
		m_renderThread->WndToFront();
	}
}

void WINAPI SetWndVisibility(HIDS* hCam, BOOL visible) 
{
	if (m_renderThread) {
		// TODO should get the thread associated with the given camera handle here..
		m_renderThread->SetWndVisibility(visible);
	}
}

INT WINAPI StartRecording(HIDS* m_hCam, const char* strFilePath, INT quality) {
	AFX_MANAGE_STATE(AfxGetStaticModuleState());
	INT result = IS_SUCCESS;
	if (!m_nAviID) {
		result = isavi_InitAVI(&m_nAviID, *m_hCam);
	}
	if (result == IS_AVI_NO_ERR) {
		// Query image buffer geometry
		int nWidth, nHeight, xPos, yPos;
		int pnX, pnY, pnBits, pnPitch;
		
		is_InquireImageMem (*m_hCam, m_pcImageMemory, m_lMemoryId, &pnX,&pnY, &pnBits, &pnPitch);
		GetImageSize( *m_hCam, nWidth, nHeight );
		// Derive pixel pitch from buffer byte pitch
		INT pPitchPx=0;
		pPitchPx = (pnPitch * 8 ) / pnBits;
		nWidth =  nWidth /8 * 8; // width has to be a multiple of 8
		INT LineOffsetPx = pPitchPx - nWidth ;

		// Get actual framerate
		double newFPS;
		GetFrameRate(*m_hCam, newFPS);
		// Get image position 
		GetImagePos(*m_hCam, xPos, yPos);
		pnX = pnX == nWidth ? 0 : xPos;
		pnY = pnY == nHeight ? 0 : yPos;
		// Get color mode
		INT cMode = GetColorMode(*m_hCam);

		result |= isavi_SetImageSize( m_nAviID, cMode,
			nWidth, nHeight,
			pnX, pnY,
			LineOffsetPx);

		result |= isavi_OpenAVI(m_nAviID, strFilePath);
		// TODO image quality is hard coded to 75 for now
		result |= isavi_SetImageQuality (m_nAviID, quality);
		result |= isavi_SetFrameRate(*m_hCam, newFPS);
		result |= isavi_StartAVI(m_nAviID);
		// Set recording to true
		m_bRecording = TRUE;
		PostThreadMessage(m_renderThread->m_nThreadID, IS_THREAD_MESSAGE, IS_RECORDING, (LPARAM) m_bRecording);
		// Send avi id to render thread
		PostThreadMessage(m_renderThread->m_nThreadID, IS_THREAD_MESSAGE, SET_AVI_ID, (LPARAM) m_nAviID);
		TRACE("Recording started\n");
	}	
	return result;
}

INT WINAPI StopRecording(HIDS* m_hCam) {
	AFX_MANAGE_STATE(AfxGetStaticModuleState());
	INT result = IS_AVI_ERR_INVALID_ID;
	if (m_bRecording && m_nAviID) {
		// set recording to false
		m_bRecording = FALSE;
		// stops the capture thread
		//subsequent calls to addAviFrame will be ignored
		PostThreadMessage(m_renderThread->m_nThreadID, IS_THREAD_MESSAGE, IS_RECORDING, (LPARAM) m_bRecording);
		result = isavi_StopAVI(m_nAviID);
		result |= isavi_CloseAVI(m_nAviID);
		result |= isavi_ResetFrameCounters(m_nAviID);
		TRACE("Recording stopped\n");
	}
	return result;
}

void WINAPI GetFrameDropInfo(HIDS* m_hCam, unsigned long* frameDropInfo) {
	if (m_bRecording && m_nAviID) {
		unsigned long compressedFrames;
		isavi_GetnCompressedFrames(m_nAviID, &compressedFrames);
		frameDropInfo[0] = compressedFrames + frameDropInfo[1];	
		isavi_GetnLostFrames(m_nAviID, &frameDropInfo[1]);
		TRACE("Recorded: %d Dropped: %d\n", frameDropInfo[0], frameDropInfo[1]);
	}
}

INT WINAPI GetCameraNames(UEYE_CAMERA_LIST* pCameraList) {
	INT nNumCam = 0;
	INT result = is_GetNumberOfCameras( &nNumCam );
	if(result == IS_SUCCESS) {
		pCameraList->dwCount = nNumCam;
		// At least one camera must be available
		if (nNumCam >= 1) {
			result |= is_GetCameraList(pCameraList);
			// Camera found
			TRACE("Camera models found %s\n", pCameraList->uci->Model);
		}
	} 	
	return result;
}

BOOL WINAPI FilterDllMsg(LPMSG lpMsg)
{
	AFX_MANAGE_STATE(AfxGetStaticModuleState());
	TRY
	{
		return AfxGetThread()->PreTranslateMessage(lpMsg);
	}
	END_TRY
	return FALSE;
}

void WINAPI ProcessDllIdle()
{
	AFX_MANAGE_STATE(AfxGetStaticModuleState());
	TRY
	{
		// flush it all at once
		long lCount = 0;
		while (AfxGetThread()->OnIdle(lCount))
			lCount++;
	}
	END_TRY
}