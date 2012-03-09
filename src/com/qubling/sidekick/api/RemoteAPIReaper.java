package com.qubling.sidekick.api;

import java.util.Collection;
import java.util.HashSet;

import android.os.AsyncTask;
import android.os.Handler;

/**
 * This provides a simple {@link Runnable} that will reap the given remote API
 * objects if they are running. This should be run via
 * {@link Handler#postDelayed(Runnable, long)}.
 *
 * @author sterling
 *
 * @param <Task> The {@link RemoteAPI} type that this will reap.
 */
public class RemoteAPIReaper<Task extends RemoteAPI<?, ?, ?>> implements Runnable {
	private Collection<Task> tasksToReap;

	public RemoteAPIReaper() {
		tasksToReap = new HashSet<Task>();
	}

	public void addTaskToReap(Task taskToReap) {
		this.tasksToReap.add(taskToReap);
	}

	@Override
    public void run() {
		for (Task t : tasksToReap) {
		    if (t.getStatus() == AsyncTask.Status.RUNNING) {
		    	t.cancel(true);
		    }
		}
    }
}
