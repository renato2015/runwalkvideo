#pragma once

#include "stdafx.h"
#include "uEyeCapturerApi.h"
#include "FullscreenWnd.h"
// CuEyeRenderThread

class CuEyeRenderThread : public CWinThread
{
	DECLARE_DYNCREATE(CuEyeRenderThread)
protected: 
	DECLARE_MESSAGE_MAP()
private:
	CFullscreenWnd* m_WndFS;
	HANDLE		m_hEvent;
	HIDS*		m_hCam;
	INT*		m_lMemoryId;	// grabber memory - buffer ID
	char*		m_pcImageMemory;// grabber memory - pointer to buffer
	BOOL		m_bRecording;
	BOOL		m_bRunning;
	INT			m_nAviID;

	HANDLE		m_hEventThread;		
	DWORD		m_dwThreadID;
	
	INT*		m_monitorId;
	HWND		m_windowHandle;
	void		(WINAPI*m_OnWindowShow) (BOOL);
	afx_msg void OnThreadMessage(WPARAM wParam, LPARAM lParam);
	BOOL		IsRunning();
	void		SetRunning(BOOL running);
	BOOL		IsRecording();
	void		SetRecording(BOOL recording);
	void		SetAviId(INT nAviID);
	CFullscreenWnd* m_mainWnd;
	// private constructor used by dynamic creation
	CuEyeRenderThread(); 
public:
	// destructor public, called from owning thread
	virtual ~CuEyeRenderThread();
	virtual BOOL InitInstance();
	virtual BOOL ExitInstance();
	//virtual int ExitInstance();
	void Initialize(HIDS* m_hCam, char* m_pcImageMemory, INT* m_lMemoryId, INT* m_monitorId, void (WINAPI*OnWindowShow)(BOOL), HWND windowHandle);
	void WndToFront();
	void SetWndVisibility(BOOL visible);
	void ThreadProc();
};


