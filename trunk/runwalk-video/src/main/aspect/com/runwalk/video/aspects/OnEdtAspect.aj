package com.runwalk.video.aspects;

import java.awt.EventQueue;

import javax.swing.SwingUtilities;

import com.runwalk.video.ui.OnEdt;

public aspect OnEdtAspect {
	  pointcut onEdt() : execution(@OnEdt * *(..));
	    
	    void around() : onEdt() {
	        if (!EventQueue.isDispatchThread()) {
	            SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                    proceed();
	                }
	            });
	        }
	    }
}
