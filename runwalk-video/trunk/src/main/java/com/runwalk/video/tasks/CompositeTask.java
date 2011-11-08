package com.runwalk.video.tasks;

import java.util.List;

import org.jdesktop.application.Task;

public class CompositeTask<T, V> extends AbstractTask<T, V> {
	
	List<Task<?, ?>> tasks;

	public CompositeTask(String name) {
		super(name);
	}

	protected T doInBackground() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean addTask(Task<?, ?> task) {
		return tasks.add(task);
	}

}
