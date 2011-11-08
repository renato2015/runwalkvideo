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
	HIDS*		m_hCam;
	INT*		m_lMemoryId;	// grabber memory - buffer ID
	char*		m_pcImageMemory;// grabber memory - pointer to buffer
	BOOL		m_bRecording;
	INT*		nAviID;
	BOOL		IsRecording();
	LPTSTR		m_windowName;
	INT*		m_monitorId;
	HWND		m_windowHandle;
	void		(WINAPI*m_OnWindowShow) (BOOL);
	afx_msg void OnThreadMessage(WPARAM wParam, LPARAM lParam);
	afx_msg void OnUEyeMessage(WPARAM wParam, LPARAM lParam);
	CFullscreenWnd* wnd;
	// private constructor used by dynamic creation
	CuEyeRenderThread(); 
protected:
	virtual ~CuEyeRenderThread();
public:
	virtual BOOL InitInstance();
	//virtual int ExitInstance();
	void Initialize(HIDS* m_hCam, char* m_pcImageMemory, INT* m_lMemoryId, LPTSTR m_windowName, INT* m_monitorId, void (WINAPI*OnWindowShow)(BOOL), HWND windowHandle);
	void CuEyeRenderThread::WndToFront();
	void CuEyeRenderThread::SetWndVisibility(BOOL visible);

};


