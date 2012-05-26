package com.qubling.sidekick.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.util.Log;

public class JobManager {
	private static class JobLoop implements Runnable {
		private static final long DEFAULT_JOB_WAIT_TIMEOUT = 10L;
		private static final TimeUnit DEFAULT_JOB_WAIT_UNIT = TimeUnit.SECONDS;
		
		private BlockingQueue<Callable<Void>> jobQueue;
		
		private long jobWaitTimeout = DEFAULT_JOB_WAIT_TIMEOUT;
		private TimeUnit jobWaitUnit = DEFAULT_JOB_WAIT_UNIT;
		
		public JobLoop(BlockingQueue<Callable<Void>> jobQueue) {
			this.jobQueue = jobQueue;
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					Callable<Void> job = jobQueue.poll(jobWaitTimeout, jobWaitUnit);
					if (job == null) return;
					job.call();
				}
				catch (InterruptedException e) {
					return;
				}
				catch (Exception e) {
					Log.e("JobManager.JobThread", "error running job", e);
				}
			}
		}
	}
	
	private static class JobControlLoop implements Runnable {
		private final JobManager parent;
		private final Queue<JobGroup> jobQueue;
		
		public JobControlLoop(JobManager parent, Queue<JobGroup> jobQueue) {
			this.parent = parent;
			this.jobQueue = new LinkedList<JobGroup>();
			this.jobQueue.addAll(jobQueue);
		}
		
		@Override
		public void run() {
			parent.executeJobsStarted();
			
			while (!jobQueue.isEmpty()) {
				final JobGroup nextJob = jobQueue.poll();
				
				Log.d("JobManager.JobControlLoop", "Start Next Job");
				
				try {
					CountDownLatch latch = nextJob.getCountDownLatch();
					
					for (final Callable<Void> callable : nextJob.getCallables()) {
						parent.addToJobQueue(callable, latch);
					}
					
					latch.await();
				}
				catch (InterruptedException e) {
					Log.e("JobManager.JobControlLoop", "UI Job was interrupted.", e);
				}
			}
			
			parent.executeJobsComplete();
		}
	}

	private static class JobGroup {
    	private int runCount;
    	private Collection<Callable<Void>> callables;
    	
    	public JobGroup(Callable<Void> callable) {
    		this.runCount = 1;
    		this.callables = new ArrayList<Callable<Void>>();
    		this.callables.add(callable);
    	}
    	
    	public JobGroup(Callable<Void>... callables) {
    		this.runCount = callables.length;
    		this.callables = new ArrayList<Callable<Void>>();
    		Collections.addAll(this.callables, callables);
    	}
    	
    	public JobGroup(Runnable... runnables) {
    		this.runCount = runnables.length;
    		this.callables = new ArrayList<Callable<Void>>();
    		
    		for (Runnable runnable : runnables) {
    			this.callables.add(Executors.callable(runnable, (Void) null));
    		}
    	}
    	
    	public JobGroup(final Runnable runnable, final Activity activity) {
    		this(new ActivityUiCallable<Void>() {
    			@Override
    			public Activity getActivity() {
    				return activity;
    			}
    			
    			@Override
    			public Void call() {
    				runnable.run();
    				return null;
    			}
			});
    	}
    	
    	public CountDownLatch getCountDownLatch() {
    		return new CountDownLatch(runCount);
    	}
    	
    	public Collection<Callable<Void>> getCallables() {
    		return Collections.unmodifiableCollection(callables);
    	}
    }
	
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
		jobQueue.offer(new JobGroup(callable));
	}
	
	public void submit(Callable<Void>... callables) {
		jobQueue.offer(new JobGroup(callables));
	}
	
	public void submit(Runnable... runnables) {
		jobQueue.offer(new JobGroup(runnables));
	}
	
	public void submit(Runnable runnable, Activity activity) {
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
			Log.d("JobManager", "Callable " + callable + " is an ActivityUiCallable");
			
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
			});
		}
		else if (callable instanceof ControlCallable<?>) {
			Log.d("JobManager", "Callable " + callable + " is a ControlCallable");
			
			jobLoopNeeded = true;
			
			controlExecutor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					((ControlCallable<Void>) callable).setJobManager(JobManager.this);
					callable.call();
					if (latch != null) latch.countDown();
					return null;
				}
			});
		}
		else {
			Log.d("JobManager", "Callable " + callable + " is a Callable");
			
			jobLoopNeeded = true;
			
			callableQueue.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					callable.call();
					if (latch != null) latch.countDown();
					return null;
				}
			});
		}
		
		if (jobLoopNeeded) 
			startJobThread();
	}
}
