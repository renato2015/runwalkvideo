package com.runwalk.video.aspects;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.SwingUtilities;

import com.runwalk.video.ui.OnEdt;

public aspect OnEdtAspect {
	pointcut invokeLater() : execution(@OnEdt+ void *(..));

	pointcut invokeAndWait() : execution(@OnEdt+ boolean *(..));

/*	 boolean around() : invokeAndWait() {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				return invokeAndWait(new Callable<Boolean>() {

					public Boolean call() throws Exception {
						return proceed();
					}
					
					
				});
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return proceed();
	}*/

	void around() : invokeLater() {
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
	
	public static <T> FutureTask<T> invokeLater(Callable<T> callable) {
	    FutureTask<T> task = new FutureTask<T>(callable);
	    SwingUtilities.invokeLater(task);
	    return task;
	}

	public static <T> T invokeAndWait(Callable<T> callable) throws InterruptedException, InvocationTargetException {
		try {
			//blocks until future returns
			return invokeLater(callable).get();
		} catch (ExecutionException e) {
			Throwable t = e.getCause();
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else if (t instanceof InvocationTargetException) {
				throw (InvocationTargetException) t;
			} else {
				throw new InvocationTargetException(t);
			}
		}
	}

}
