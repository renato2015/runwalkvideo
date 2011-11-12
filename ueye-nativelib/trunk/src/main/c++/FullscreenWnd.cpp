#include "stdafx.h"
#include "FullscreenWnd.h"

#include <math.h>
#include ".\fullscreenwnd.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

CFullscreenWnd::CFullscreenWnd(HIDS* hCam, char* pcImageMemory,  INT* lMemoryId, INT pixelsX, INT pixelsY, void (WINAPI*OnWindowShow)(BOOL))
{
  m_hCam = hCam;
  m_pcImageMemory = pcImageMemory;
  m_lMemoryId = *lMemoryId;
  m_OnWindowShow = OnWindowShow;

  m_pixelsX = pixelsX;
  m_pixelsY = pixelsY;

  m_bShowTime = FALSE;
  m_bShowFPS = FALSE;
  m_bClear = FALSE;
}

CFullscreenWnd::~CFullscreenWnd()
{
	// set all pointers to null
	m_hCam = NULL;
	m_pcImageMemory = NULL;
	m_lMemoryId = NULL;
}

void CFullscreenWnd::OnShowWindow(BOOL bShow, UINT nStatus) {
	(*m_OnWindowShow)(bShow);
}

void CFullscreenWnd::OnSetFocus( CWnd* wnd) {
	// TODO call java funcion pointer
}

BEGIN_MESSAGE_MAP(CFullscreenWnd, CWnd)
	ON_WM_SYSCOMMAND()
	ON_WM_SHOWWINDOW() 
	ON_WM_SETFOCUS()
	ON_WM_ERASEBKGND()
	ON_WM_PAINT()
	ON_WM_KEYDOWN()
	ON_MESSAGE(IS_UEYE_MESSAGE, OnUEyeMessage)
	ON_WM_TIMER()
	ON_WM_ACTIVATE()
END_MESSAGE_MAP()


///////////////////////////////////////////////////////////////////////////////
//
// METHOD CIdsSimpleLiveDlg::OnUEyeMessage() 
//
// DESCRIPTION: - handles the messages from the uEye camera
//				- messages must be enabled using is_EnableMessage()
//
///////////////////////////////////////////////////////////////////////////////
LRESULT CFullscreenWnd::OnUEyeMessage( WPARAM wParam, LPARAM lParam )
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
			is_RenderBitmap( *m_hCam, m_lMemoryId, *this, IS_RENDER_NORMAL );
			if (m_bRecording && nAviID != NULL) {
				isavi_AddFrame(*nAviID, m_pcImageMemory);
			}
		} else {
			 AfxMessageBox(TEXT("No memory allocated for drawing"), MB_ICONWARNING );
		}
      break;
  }    
  return 0;
}

/////////////////////////////////////////////////////////////////////////////
// CFullscreenWnd message handlers

BOOL CFullscreenWnd::OnEraseBkgnd(CDC* pDC) 
{
    m_backColor = RGB(0,0,0);

    CBrush cb(m_backColor);
    HBRUSH hOldBrush = (HBRUSH)pDC->SelectObject(cb);
    RECT rect = {0,0,m_pixelsX,m_pixelsY};
    pDC->FillRect(&rect,&cb);
    pDC->SelectObject(hOldBrush);
    cb.DeleteObject();
 
    return TRUE;
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CFullscreenWnd::OnPaint() 
{
	if (IsIconic())
	{

		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, (WPARAM) dc.GetSafeHdc(), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;
		// Draw the icon
		//dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CWnd::OnPaint();
	}
}

void CFullscreenWnd::OnKeyDown (UINT nChar, UINT nRepCnt, UINT nFlags) 
{
  switch (nChar)
  {
    case VK_ESCAPE:
      ShowWindow(SW_HIDE);
      break;

    case 0x31:
      m_bShowTime = !m_bShowTime;
      m_bClear = TRUE;
      break;
    
    case 0x32:
      m_bShowFPS = !m_bShowFPS;
      m_bClear = TRUE;
      break;

  }
    
    if (m_bShowTime || m_bShowFPS)
    {
        is_DirectRenderer (*m_hCam, DR_SHOW_OVERLAY, NULL, NULL);
    }
    else
    {
        is_DirectRenderer (*m_hCam, DR_HIDE_OVERLAY, NULL, NULL);
    }
 
	CWnd::OnKeyDown(nChar, nRepCnt, nFlags);
}

void CFullscreenWnd::OnActivate(UINT nState, CWnd* pWndOther, BOOL bMinimized) 
{
    CWnd::OnActivate(nState, pWndOther, bMinimized);
    
    // TODO: Add your message handler code here
    if(nState != WA_INACTIVE ) 
    {
        SetCursor(m_hCursor);
        ShowCursor(true);
    }
}

BOOL CFullscreenWnd::SetFrameRate (double fps)
{
    BOOL bRet = FALSE;
    if (/*m_pView && */ IsWindowEnabled() && IsWindowVisible())
    {
        bRet = TRUE;

        if (m_bShowTime || m_bShowFPS || m_bClear)
        {
            // variables
	        SYSTEMTIME st;
            CString str;

            CDC* pdc;
            HDC hdc;
            if (is_DirectRenderer (*m_hCam, DR_GET_OVERLAY_DC, (void*)&hdc, sizeof (hdc)) == IS_SUCCESS)
            {
                pdc = CDC::FromHandle (hdc);
                if(pdc != NULL)
                {
                    if (m_bClear)
                    {
                        CRect rect;
                        GetClientRect (&rect);
                        CBrush br (RGB (0, 0, 0));
                        pdc->FillRect(&rect, &br);
                        m_bClear = FALSE;
                        DeleteObject (br);
                    }

                    SetBkColor (pdc->m_hDC, RGB (0, 0 ,0));

                    // red text
                    SetTextColor (pdc->m_hDC, RGB (255, 0, 0));

                    if (m_bShowTime)
                    {
                        // get time
                        GetLocalTime (&st);

                        // text output
                        str.Format(L"%02d:%02d:%02d" , st.wHour, st.wMinute, st.wSecond);
                        TextOut (pdc->m_hDC, 10, 10, str, str.GetLength());
                    }

                    if (m_bShowFPS)
                    {
                        RECT r;
                        GetClientRect(&r);

                        CString strTemp;
		             /*   strTemp.LoadString(ID_FRAMES_PER_SECOND2);

                        str.Format(L"%5.1f %s", fps, strTemp);*/

                        INT x = ((r.right - r.left) / 2) - (pdc->GetTextExtent(str).cx) - 10;
                        INT y = 10;

                        TextOut (pdc->m_hDC, x, y, str, str.GetLength());
                    }

                    ReleaseDC(pdc);
                }

                is_DirectRenderer (*m_hCam, DR_RELEASE_OVERLAY_DC, (void*)&hdc, sizeof (hdc));
            }
        }
    }
    
    return bRet;
}