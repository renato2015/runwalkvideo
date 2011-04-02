package com.runwalk.video.aspects;

import java.util.concurrent.TimeUnit;
import org.jdesktop.application.Task;
import org.apache.log4j.Logger;

public aspect BenchmarkInterceptor {

    after() returning : execution(* com.runwalk.video.tasks.AbstractTask.doInBackground()) {
    	Task<?, ?> task = (Task<?, ?>) thisJoinPoint.getTarget();
    	long ms = task.getExecutionDuration(TimeUnit.MILLISECONDS);
    	long s = task.getExecutionDuration(TimeUnit.SECONDS);
    	String duration = ms > 10000 ? s + "s" : ms + "ms";
    	Logger.getLogger(BenchmarkInterceptor.class).debug(task.getClass().getSimpleName() + " finished in " + duration);
     }
	
}
