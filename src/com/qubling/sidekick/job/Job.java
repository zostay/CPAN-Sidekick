package com.qubling.sidekick.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;
import android.util.Log;

public class Job implements Runnable {
	private Collection<Runnable> commands;
	private final Activity activity;
	
	public Job(Activity activity) {
	    this.activity = activity;
	    this.commands = new ArrayList<Runnable>();
    }
	
	public void addCommand(Runnable command) {
		addCommand(command, null);
	}
	
	public void addCommand(final Runnable command, final Runnable followup) {
		if (followup != null) {
			commands.add(new Runnable() {
				@Override
				public void run() {
					command.run();
					activity.runOnUiThread(followup);
				}
				
				@Override
				public String toString() {
					return "Runnable " + command + " Followed By " + followup;
				}
			});
		}
		else {
			commands.add(command);
		}
	}

	@Override
	public void run() {
		LinkedList<Runnable> commandsCopy = new LinkedList<Runnable>(commands);
		final CountDownLatch latch = new CountDownLatch(commandsCopy.size());
		while (!commandsCopy.isEmpty()) {
			final Runnable command = commandsCopy.poll();
			JobExecutor job = new JobExecutor(activity);
			job.addCommand(new Runnable() {
				
				@Override
				public void run() {
					command.run();
					latch.countDown();
				}
				
				@Override
				public String toString() {
					return "Latched " + command;
				}
			});
			
			try {
				job.executeOnExecutor(JobExecutor.THREAD_POOL_EXECUTOR);
			}
			catch (RejectedExecutionException e) {
				Log.e("Job", "Failed to start job in parallel, will try again.", e);
				commandsCopy.offer(command);
			}
		}
		
		try {
			latch.await();
		}
		catch (InterruptedException e) {
			Log.e("Job", "Interrupted while waiting for jobs to finish.", e);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder commandList = new StringBuilder();
		for (Runnable command : commands) {
			commandList.append(command);
			commandList.append(",");
		}
		return "Job " + commandList;
	}
}
