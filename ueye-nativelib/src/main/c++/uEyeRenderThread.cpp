// CuEyeRenderThread.cpp : implementation file
//

#include "stdafx.h"
#include "uEyeCapturer.h"
#include "uEyeRenderThread.h"
#include "Monitors.h"
#include "MultiMonitor.h"

// CuEyeRenderThread

IMPLEMENT_DYNCREATE(CuEyeRenderThread, CWinThread)

BEGIN_MESSAGE_MAP(CuEyeRenderThread, CWinThread)
	ON_THREAD_MESSAGE(IS_UEYE_MESSAGE, OnUEyeMessage)
	ON_THREAD_MESSAGE(IS_THREAD_MESSAGE, OnThreadMessage)
END_MESSAGE_MAP()

CuEyeRenderThread::CuEyeRenderThread() {	
	// default constructor
}

CuEyeRenderThread::~CuEyeRenderThread()
{
	if (wnd != NULL) {
		// run the FullScreenWnd destructor
		delete wnd;
	}
	// set all pointers to null
	if (m_windowName != NULL) {
		delete m_windowName;
		m_windowName = NULL;
	}
	nAviID = NULL;
	m_hCam = NULL;
	m_pcImageMemory = NULL;
	m_lMemoryId = NULL;
	m_windowHandle = NULL;
}

void CuEyeRenderThread::Initialize(HIDS* hCam, char* pcImageMemory, INT* lMemoryId, LPTSTR windowName, INT* monitorId, void (WINAPI*OnWindowShow)(BOOL), HWND windowHandle)
{
  m_hCam = hCam;
  m_pcImageMemory = pcImageMemory;
  m_lMemoryId = lMemoryId;
  m_windowName = windowName;
  m_monitorId = monitorId;
  m_OnWindowShow = OnWindowShow;
  m_windowHandle = windowHandle;
}

void CuEyeRenderThread::WndToFront() {
	SetForegroundWindow(m_pMainWnd->GetSafeHwnd());
	BringWindowToTop(m_pMainWnd->GetSafeHwnd());
}

void CuEyeRenderThread::SetWndVisibility(BOOL visible)
{
	INT flags = visible ? SW_SHOW : SW_HIDE;
	m_pMainWnd->ShowWindow(flags);
}

BOOL CuEyeRenderThread::InitInstance()
{
	BOOL nRet = 0;
	// create new window if no handle was initialized
	if (m_windowHandle == NULL) {
		CRect rect;
		// if a monitor was selected, then move the window over there
		if (m_monitorId != NULL) {
			CMonitors monitors;
			CMonitor monitor = monitors.GetMonitor(*m_monitorId);
			monitor.GetMonitorRect(rect);
		}
		wnd = new CFullscreenWnd(m_hCam, m_pcImageMemory, m_lMemoryId, rect.Width(), rect.Height(), m_OnWindowShow);
		m_pMainWnd = wnd;
		CString csWndClass = AfxRegisterWndClass(CS_HREDRAW|CS_VREDRAW, 0, 0, 0);
		//const char* myWndClass = AfxRegisterWndClass(CS_HREDRAW|CS_VREDRAW, NULL);
	
		// got rectangle here
		nRet &= ((*m_pMainWnd).CreateEx( WS_EX_LEFT /*| WS_EX_TOPMOST*/,
			(LPCTSTR)csWndClass,
			m_windowName,
			WS_POPUP | WS_VISIBLE,
			//0, 0, 0, 0,
			rect.left,rect.top,rect.right - rect.left,rect.bottom - rect.top,    
			//rect,
			NULL,
			NULL
		)); 

		// if a monitor was selected, then move the window over there
		if (m_monitorId != NULL) {
			SetWindowPos(m_pMainWnd->GetSafeHwnd(), NULL, rect.left, rect.top, rect.Width(), rect.Height(), SWP_SHOWWINDOW | SWP_FRAMECHANGED);	
			//ShowWindow(m_pMainWnd->GetSafeHwnd(), SW_MAXIMIZE);
			//monitor.CenterWindowToMonitor(m_pMainWnd, FALSE);
		}
		m_windowHandle = m_pMainWnd->GetSafeHwnd();
		// Enable Messages
		nRet &= is_EnableMessage(*m_hCam, IS_DEVICE_REMOVED, m_windowHandle);
		nRet &= is_EnableMessage(*m_hCam, IS_DEVICE_RECONNECTED, m_windowHandle);
		nRet &= is_EnableMessage(*m_hCam, IS_FRAME, m_windowHandle);	
		nRet &= is_CaptureVideo( *m_hCam, IS_WAIT );
	} else {
		// start live video
		
		// start event loop here 
		HANDLE hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
		is_InitEvent(*m_hCam, hEvent, IS_SET_EVENT_FRAME);
		is_EnableEvent(*m_hCam, IS_SET_EVENT_FRAME);
		is_CaptureVideo( *m_hCam, IS_DONT_WAIT );
		while(true) {
			DWORD dwRet = WaitForSingleObject(hEvent, 1000);
			if (dwRet == WAIT_TIMEOUT) {
				// wait timed out
			} else if (dwRet == WAIT_OBJECT_0) {
				// event signalled
				is_RenderBitmap( *m_hCam, *m_lMemoryId, m_windowHandle, IS_RENDER_NORMAL );
			}
		}
		
		//is_DisableEvent(hCam, IS_SET_EVENT_FRAME);
		//is_ExitEvent(hCam, IS_SET_EVENT_FRAME);
	}

	return TRUE;
}

void CuEyeRenderThread::OnThreadMessage(WPARAM wParam, LPARAM lParam) {
	switch(wParam) {
		case SET_AVI_ID: {
			//SetAviId((INT*) lParam);
			break;
		}
		case IS_RECORDING: {
			//SetRecording((BOOL) lParam);
			break;
		}	
		case STOP_RUNNING: {
			PostQuitMessage(0);
			break;
		}
	}
}

///////////////////////////////////////////////////////////////////////////////
//
// METHOD CIdsSimpleLiveDlg::OnUEyeMessage() 
//
// DESCRIPTION: - handles the messages from the uEye camera
//				- messages must be enabled using is_EnableMessage()
//
///////////////////////////////////////////////////////////////////////////////
void CuEyeRenderThread::OnUEyeMessage( WPARAM wParam, LPARAM lParam )
{
  switch ( wParam )
  {
      case IS_DEVICE_REMOVED:
          Beep( 400, 50 );
          break;
      case IS_DEVICE_RECONNECTED:
          Beep( 400, 50 );
          break;
      case IS_FRAME:
		if( m_pcImageMemory != NULL ) {
			is_RenderBitmap( *m_hCam, *m_lMemoryId, m_windowHandle, IS_RENDER_NORMAL );
			if (m_bRecording && nAviID != NULL) {
				isavi_AddFrame(*nAviID, m_pcImageMemory);
			}
		} else {
			 AfxMessageBox(TEXT("No memory allocated for drawing"), MB_ICONWARNING );
		}
      break;
  }   
}


// CuEyeRenderThread message handlers
