package com.runwalk.video.glazedlists;

import javax.swing.SwingUtilities;

import org.jdesktop.application.TaskService;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.gui.ThreadProxyEventList;

import com.runwalk.video.tasks.AbstractTask;

public class TaskProxyEventList<T> extends ThreadProxyEventList<T> {
	
	private final TaskService taskService;

	public TaskProxyEventList(TaskService taskService, EventList<T> source) {
		super(source);
		this.taskService = taskService;
	}
	
	protected void schedule(final Runnable runnable) {
		// don't build this part of the pipeline on the EDT (lazy loading)
		if (SwingUtilities.isEventDispatchThread()) {
			getTaskService().execute(new AbstractTask<Void, Void>("loadEntities") {
				
				protected Void doInBackground() throws Exception {
					runnable.run();
					return null;
				}
				
			});
		} else {
			runnable.run();
		}
	}
	
	private TaskService getTaskService() {
		return taskService;
	}
	
}