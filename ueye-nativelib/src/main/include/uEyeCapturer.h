// CuEyeCapturer.h : main header file for the CuEyeCapturer DLL
//

#pragma once

#ifndef __AFXWIN_H__
	#error "include 'stdafx.h' before including this file for PCH"
#endif

#include "resource.h"		// main symbols


// CCuEyeCapturerApp
// See CuEyeCapturer.cpp for the implementation of this class
//

class CCuEyeCapturerApp : public CWinApp
{
public:
	CCuEyeCapturerApp();

// Overrides
public:
	virtual BOOL InitInstance();

	DECLARE_MESSAGE_MAP()
};
