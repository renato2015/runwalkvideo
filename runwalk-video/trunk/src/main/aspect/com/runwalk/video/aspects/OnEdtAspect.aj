package com.runwalk.video.aspects;

import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.SwingUtilities;

import com.runwalk.video.core.OnEdt;
import com.runwalk.video.ui.CalendarSlotDialog;

public aspect OnEdtAspect {
	
	pointcut invokeLater() : execution(@OnEdt+ void *(..));

	pointcut invokeCallableLater() : execution(@OnEdt+ FutureTask<? extends Window> *(..));
	
	/**
	 * This advice is applied to methods that create a {@link Window}, which should always happen on the EDT.
	 * The window created by the function will be returned after it is instantiated.
	 * @return The created window.
	 */
	FutureTask<? extends Window> around() : invokeCallableLater() {
		FutureTask<? extends Window> futureTask = proceed();
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(futureTask);
		} else {
			futureTask.run();
		}
		return futureTask;
	}

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
