package com.qubling.sidekick.job;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;



import android.app.Activity;
import android.util.Log;

public class JobManager {
	public interface Monitor {
		public void executeJobsStarted();
		public void executeJobsComplete();
	}

	private final Queue<JobGroup> jobQueue;
	private final BlockingQueue<Callable<Void>> callableQueue;
	private final ExecutorService controlExecutor;
	private final ExecutorService jobExecutor;
	private Monitor monitor;

	public JobManager(ExecutorService controlExecutor, ExecutorService jobExecutor) {
		this.controlExecutor = controlExecutor;
		this.jobExecutor = jobExecutor;
		
		jobQueue = new LinkedList<JobGroup>();
		callableQueue = new LinkedBlockingQueue<Callable<Void>>();
	}
	
	public void setJobManagerMonitor(Monitor monitor) {
		this.monitor = monitor;
	}
	
	public void submit(Callable<Void> callable) {
		Log.d("JobManager", "Submit job " + callable);
		jobQueue.offer(new JobGroup(callable));
	}
	
	public void submit(Callable<Void>... callables) {
		for (Callable<Void> callable : callables)
			Log.d("JobManager", "Submit job " + callable);
		jobQueue.offer(new JobGroup(callables));
	}
	
	public void submit(Runnable... runnables) {
		for (Runnable runnable : runnables)
			Log.d("JobManager", "Submit job " + runnable);
		jobQueue.offer(new JobGroup(runnables));
	}
	
	public void submit(Runnable runnable, Activity activity) {
		Log.d("JobManager", "Submit UI job " + runnable);
		jobQueue.offer(new JobGroup(runnable, activity));
	}
	
	public void executeJobs() {
		controlExecutor.submit(new JobControlLoop(this, jobQueue));
	}
	
	protected void executeJobsStarted() { 
		if (monitor != null) monitor.executeJobsStarted();
	}
	
	protected void executeJobsComplete() { 
		if (monitor != null) monitor.executeJobsComplete();
	}
	
	private void startJobThread() {
		try {
			jobExecutor.submit(new JobLoop(callableQueue));
		}
		catch (RejectedExecutionException e) {
			// Pretty normal occurrence. The ExecutorService is letting us 
			// know we've already used up all the threads in the pool. As
			// long as there's a single thread out there, we should be ok.
		}
	}
	
	public void addToJobQueue(Callable<Void> callable) {
		addToJobQueue(callable, null);
	}
	
	public void addToJobQueue(final Callable<Void> callable, final CountDownLatch latch) {
		boolean jobLoopNeeded = false;
		
		if (callable instanceof ActivityUiCallable<?>) {
//			Log.d("JobManager", "Callable " + callable + " is an ActivityUiCallable");
			
			Activity activity = ((ActivityUiCallable<?>) callable).getActivity();
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						callable.call();
						if (latch != null) latch.countDown();
					}
					catch (Exception e) {
						Log.e("JobManager", "error calling callable job in UI thread", e);
					}
				}
				
				@Override
				public String toString() {
					return "addToJobQueue:ActivityUiCallable:Runnable(" + callable + ")";
				}
			});
		}
		else if (callable instanceof ControlCallable<?>) {
//			Log.d("JobManager", "Callable " + callable + " is a ControlCallable");
			
			jobLoopNeeded = true;
			
			Log.d("JobManager", "Spawning new control thread");
			controlExecutor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					((ControlCallable<Void>) callable).setJobManager(JobManager.this);
					callable.call();
					if (latch != null) latch.countDown();
					return null;
				}
				
				@Override
				public String toString() {
					return "addToJobQueue:ControlCallable:Callable(" + callable + ")";
				}
			});
		}
		else {
//			Log.d("JobManager", "Callable " + callable + " is a Callable");
			
			jobLoopNeeded = true;
			
			Log.d("JobManager", "Submitting new Callable to the Job loop");
			try {
				callableQueue.put(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						callable.call();
						if (latch != null) latch.countDown();
						return null;
					}
					
					@Override
					public String toString() {
						return "addToJobQueue:Callable:Callable(" + callable + ")";
					}
				});
			}
			catch (InterruptedException e) {
				Log.e("JobManager", "Interrupted while putting a callable into the queue", e);
			}
		}
		
		Log.d("JobManager", "Callable Q contains " + callableQueue.size() + " " + jobLoopNeeded);
		
		if (jobLoopNeeded)  {
			startJobThread();
		}
	}
}
