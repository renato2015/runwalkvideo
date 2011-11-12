#if !defined(__FULLSCREEN_WND__)
#define __FULLSCREEN_WND__

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "uEyeCapturerApi.h"
//
/////////////////////////////////////////////////////////////////////////////
// CFullscreenWnd window

class CFullscreenWnd : public CWnd
{
// Construction
public:
	CFullscreenWnd::CFullscreenWnd(HIDS* m_hCam, char*	m_pcImageMemory,  INT* m_lMemoryId, INT pixelsX, INT pixelsY, void (WINAPI*OnWindowShow)(BOOL) );

// Attributes
public:
    int         m_pixelsX;
    int         m_pixelsY;
    int         m_bitsPixel;
    CDC*        m_pDC;
    HCURSOR     m_hCursor; 
    COLORREF    m_backColor;
    BOOL        SetFrameRate (double fps);

// Oper
// Implementation
	virtual ~CFullscreenWnd();

	// Generated message map functions
protected:
	afx_msg BOOL OnEraseBkgnd(CDC* pDC);
	afx_msg void OnPaint();

	afx_msg void OnKeyDown(UINT nChar, UINT nRepCnt, UINT nFlags);
	afx_msg void OnActivate(UINT nState, CWnd* pWndOther, BOOL bMinimized);
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void OnSetFocus( CWnd* );

	DECLARE_MESSAGE_MAP()

private:
  BOOL          m_bClear;
  BOOL          m_bShowTime;
  BOOL          m_bShowFPS;
  LRESULT		OnUEyeMessage(WPARAM wParam, LPARAM lParam);
  BOOL			m_bRecording;
  INT*			nAviID;
  HIDS*			m_hCam;
  INT			m_lMemoryId;	// grabber memory - buffer ID
  char*			m_pcImageMemory;// grabber memory - pointer to buffer
  void			(WINAPI*m_OnWindowShow) (BOOL);
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(__FULLSCREEN_WND__)
