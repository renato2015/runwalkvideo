package com.runwalk.video.aspects;

import javax.swing.SwingUtilities;

import com.runwalk.video.ui.OnEdt;

public aspect OnEdtAspect {
	  pointcut onEdt() : execution(@OnEdt+ * *(..));
	    
	    void around() : onEdt() {
	        if (!SwingUtilities.isEventDispatchThread()) {
	            SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                    proceed();
	                }
	            });
	        } else {
	        	proceed();
	        }
	    }
}
