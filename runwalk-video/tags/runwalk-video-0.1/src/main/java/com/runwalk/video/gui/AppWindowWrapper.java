package com.runwalk.video.gui;

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.util.EventListener;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.tomtessier.scrollabledesktop.BaseInternalFrame;

/**
 * This class will wrap a {@link BaseInternalFrame} or {@link Window} for use with the application's {@link VideoMenuBar}
 * 
 * @author Jeroen Peelaerts
 *
 */
public interface AppWindowWrapper extends AppComponent {
	
	public Container getHolder();
	
	public String getTitle();
	
	public void addAppWindowWrapperListener(AppWindowWrapperListener listener);
	
	public void removeAppWindowWrapperListener(AppWindowWrapperListener listener);
	
	public List<AppWindowWrapperListener> getAppWindowWrapperListeners();

	/**
	 * This class is a universal event listener for {@link JInternalFrame} and {@link Window} components. 
	 * It delegates calls to their listener interfaces to a corresponding handling method, which can be overridden.
	 * 
	 * @author Jeroen Peelaerts
	 *
	 */
	public static abstract class AppWindowWrapperListener implements WindowListener, WindowFocusListener, InternalFrameListener, EventListener {
		
		public void appWindowOpened(AWTEvent event) {}

		public void appWindowActivated(AWTEvent event) {}
		
		public void appWindowClosed(AWTEvent event) {}
		
		public void appWindowDeactivated(AWTEvent event) {}
		
		public void appWindowGainedFocus(AWTEvent event) {}
		
		/*
		 * WindowListener methods
		 */
		
		public void windowOpened(WindowEvent e) {
			appWindowOpened(e);
		}
		
		public void windowActivated(WindowEvent e) {
			appWindowActivated(e);
		}

		public void windowClosed(WindowEvent e) {
			appWindowClosed(e);
		}
		
		public void windowDeactivated(WindowEvent e) {
			appWindowDeactivated(e);
		}
		
		public void windowGainedFocus(WindowEvent e) {
			appWindowGainedFocus(e);
		}
		
		/*
		 * InternalFrameListener methods
		 */

		public void internalFrameOpened(InternalFrameEvent e) {
			appWindowOpened(e);
		}

		public void internalFrameActivated(InternalFrameEvent e) {
			appWindowActivated(e);
		}

		public void internalFrameClosed(InternalFrameEvent e) {
			appWindowClosed(e);
		}

		public void internalFrameDeactivated(InternalFrameEvent e) {
			appWindowDeactivated(e);
		}
		
		/*
		 * Unused WindowListener methods
		 */
		
		public void windowClosing(WindowEvent e) {}
		
		public void windowDeiconified(WindowEvent e) {}
		
		public void windowIconified(WindowEvent e) {}
		
		public void windowLostFocus(WindowEvent e) {}

		/*
		 * Unused InternalFrameListener methods
		 */
		
		public void internalFrameClosing(InternalFrameEvent e) {}

		public void internalFrameDeiconified(InternalFrameEvent e) {}

		public void internalFrameIconified(InternalFrameEvent e) {}

		
	}
}
