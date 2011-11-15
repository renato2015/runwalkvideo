// CuEyeRenderThread.cpp : implementation file
//

#include "stdafx.h"
#include "uEyeCapturer.h"
#include "uEyeRenderThread.h"
#include "Monitors.h"
#include "MultiMonitor.h"

// thread proc forward declaration
unsigned WINAPI threadProc(void* pv);
// CuEyeRenderThread

IMPLEMENT_DYNCREATE(CuEyeRenderThread, CWinThread)

BEGIN_MESSAGE_MAP(CuEyeRenderThread, CWinThread)
	ON_THREAD_MESSAGE(IS_THREAD_MESSAGE, OnThreadMessage)
END_MESSAGE_MAP()

CuEyeRenderThread::CuEyeRenderThread() {	
	// default constructor
}

CuEyeRenderThread::~CuEyeRenderThread()
{
	// cleanup moved to ExitInstance
}

void CuEyeRenderThread::Initialize(HIDS* hCam, char* pcImageMemory, INT* lMemoryId, INT* monitorId, void (WINAPI*OnWindowShow)(BOOL), HWND windowHandle)
{
  m_hCam = hCam;
  m_pcImageMemory = pcImageMemory;
  m_lMemoryId = lMemoryId;
  m_monitorId = monitorId;
  m_OnWindowShow = OnWindowShow;
  m_windowHandle = windowHandle;
  m_nAviID = 0;
}

BOOL CuEyeRenderThread::ExitInstance() 
{
	SetRunning(FALSE);
	// disable event firing
	is_DisableEvent(*m_hCam, IS_SET_EVENT_FRAME);
	is_ExitEvent(*m_hCam, IS_SET_EVENT_FRAME);
	// wait for the event thread to terminate
	if (WaitForSingleObject (m_hEventThread, 1000) != WAIT_OBJECT_0)
	{
		// finally terminate thread if it does not by itself
		TerminateThread (m_hEventThread, 0);
	}
	CloseHandle (m_hEventThread);
	m_hEventThread = NULL;

	CloseHandle(m_hEvent);
	// clean up window if it was created by this thread
	if (m_mainWnd && m_mainWnd->GetSafeHwnd() != m_windowHandle) {
		// run destructor
		delete m_mainWnd;
		m_mainWnd = NULL;
	}
	// set all pointers to null
	m_hCam = NULL;
	m_pcImageMemory = NULL;
	m_lMemoryId = NULL;
	m_windowHandle = NULL;
	return TRUE;
}

BOOL CuEyeRenderThread::InitInstance()
{
	BOOL nRet = 0;
	// create new window if no handle was initialized
	if (!m_windowHandle) {
		CRect rect;
		// if a monitor was selected, then move the window over there
		if (m_monitorId != NULL) {
			CMonitors monitors;
			CMonitor monitor = monitors.GetMonitor(*m_monitorId);
			monitor.GetMonitorRect(rect);
		}
		m_pMainWnd = new CFullscreenWnd(m_hCam, m_pcImageMemory, m_lMemoryId, rect.Width(), rect.Height(), m_OnWindowShow);
		CString csWndClass = AfxRegisterWndClass(CS_HREDRAW|CS_VREDRAW, 0, 0, 0);
		SENSORINFO sensorInfo;
		is_GetSensorInfo(*m_hCam, &sensorInfo);
		CString windowName(sensorInfo.strSensorName);
		// got rectangle here
		nRet &= ((*m_mainWnd).CreateEx( WS_EX_LEFT /*| WS_EX_TOPMOST*/,
			(LPCTSTR)csWndClass,
			windowName,
			WS_POPUP | WS_VISIBLE,
			rect.left,rect.top,rect.right - rect.left,rect.bottom - rect.top,    
			NULL,
			NULL
		)); 

		// if a monitor was selected, then move the window over there
		if (m_monitorId) {
			SetWindowPos(m_pMainWnd->GetSafeHwnd(), NULL, rect.left, rect.top, rect.Width(), rect.Height(), SWP_SHOWWINDOW | SWP_FRAMECHANGED);	
			//ShowWindow(m_pMainWnd->GetSafeHwnd(), SW_MAXIMIZE);
			//monitor.CenterWindowToMonitor(m_pMainWnd, FALSE);
		}
		m_windowHandle = m_pMainWnd->GetSafeHwnd();	
	} 
	// start event loop here 
	m_hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	is_InitEvent(*m_hCam, m_hEvent, IS_SET_EVENT_FRAME);
	is_EnableEvent(*m_hCam, IS_SET_EVENT_FRAME);
	//is_EnableEvent(*m_hCam, IS_SET_EVENT_REMOVE);
	//is_EnableEvent(*m_hCam, IS_SET_EVENT_NEW_DEVICE);
	SetRunning(TRUE);
	SetRecording(FALSE);
	// create event signalling thread
	m_hEventThread = (HANDLE)_beginthreadex(NULL, 0, threadProc, (void*)this, 0, (UINT*)&m_dwThreadID);
	if(!m_hEventThread)
	{
		//AfxMessageBox( "ERROR: Cannot create event tread!" , MB_ICONEXCLAMATION, 0 );
		return FALSE;
	}
	// start live video
	is_CaptureVideo( *m_hCam, IS_DONT_WAIT );
	return TRUE;
}

void CuEyeRenderThread::ThreadProc()
{
	TRACE("Rendering threadProc started\n");
	do
	{
		DWORD dwRet = WaitForSingleObject(m_hEvent, 1000);
		if (IsRunning()) {
			if (dwRet == WAIT_TIMEOUT) {
			// wait timed out
			} else if (dwRet == WAIT_OBJECT_0) {
			// event signalled
				is_RenderBitmap( *m_hCam, *m_lMemoryId, m_windowHandle, IS_RENDER_NORMAL );
				if (m_bRecording && m_nAviID) {
					INT result = isavi_AddFrame(m_nAviID, m_pcImageMemory);
					if (result != IS_AVI_NO_ERR) {
						TRACE("Adding frame failed (%i)", result);
					}
				}
			}	
		}
	}
	while(IsRunning()); 	
	TRACE("Rendering threadProc stopped\n");
}


void CuEyeRenderThread::SetRecording(BOOL recording) {
	this->m_bRecording = recording;
}

BOOL CuEyeRenderThread::IsRunning() {
	return this->m_bRunning;
}

void CuEyeRenderThread::SetRunning(BOOL running) {
	this->m_bRunning = running;
}

void CuEyeRenderThread::SetAviId(INT nAviID) {
	this->m_nAviID = nAviID;
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

void CuEyeRenderThread::OnThreadMessage(WPARAM wParam, LPARAM lParam) {
	switch(wParam) {
		case SET_AVI_ID: {
			SetAviId((INT) lParam);
			break;
		}
		case IS_RECORDING: {
			SetRecording((BOOL) lParam);
			break;
		}	
		case IS_RUNNING: {
			SetRunning((BOOL) lParam);
			PostQuitMessage(0);
			break;
		}
	}
}

unsigned WINAPI threadProc(void* pv)
{
	CuEyeRenderThread* p = (CuEyeRenderThread*)pv;
	p->ThreadProc();
	_endthreadex(0);
	return 0;
}
