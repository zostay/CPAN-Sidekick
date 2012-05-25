package com.qubling.sidekick.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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

public class JobManager<Thing> {
	private static class JobLoop<Thing> implements Runnable {
		private static final long DEFAULT_JOB_WAIT_TIMEOUT = 10L;
		private static final TimeUnit DEFAULT_JOB_WAIT_UNIT = TimeUnit.SECONDS;
		
		private JobControlLoop<Thing> controlLoop;
		private BlockingQueue<Callable<Thing>> jobQueue;
		
		private long jobWaitTimeout = DEFAULT_JOB_WAIT_TIMEOUT;
		private TimeUnit jobWaitUnit = DEFAULT_JOB_WAIT_UNIT;
		
		public JobLoop(JobControlLoop<Thing> controlLoop, BlockingQueue<Callable<Thing>> jobQueue) {
			this.controlLoop = controlLoop;
			this.jobQueue = jobQueue;
		}
		
		@Override
		public void run() {
			while (true) {
				try {
					Callable<Thing> job = jobQueue.poll(jobWaitTimeout, jobWaitUnit);
					if (job == null) return;
					Thing result = job.call();
					controlLoop.addResult(result);
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
	
	private static class JobControlLoop<Thing> implements Runnable {
		private final JobManager<Thing> parent;
		private final BlockingQueue<Callable<Thing>> callableQueue;
		private final Queue<JobGroup<Thing>> jobQueue;
		private final List<Thing> currentResults;
		
		public JobControlLoop(JobManager<Thing> parent, Queue<JobGroup<Thing>> jobQueue) {
			this.parent = parent;
			this.jobQueue = new LinkedList<JobGroup<Thing>>();
			this.jobQueue.addAll(jobQueue);
			this.callableQueue = new LinkedBlockingQueue<Callable<Thing>>();
			this.currentResults = Collections.synchronizedList(new ArrayList<Thing>());
		}
		
		@Override
		public void run() {
			parent.executeJobsStarted();
			
			while (!jobQueue.isEmpty()) {
				final JobGroup<Thing> nextJob = jobQueue.poll();
				
				Log.d("JobManager.JobControlLoop", "Start Next Job");
				
				try {
					int jobLoopsNeeded = 0;
					final CountDownLatch latch = nextJob.getCountDownLatch();
					
					for (final Callable<Thing> callable : nextJob.getCallables()) {
						if (callable instanceof ActivityUiCallable<?>) {
							Activity activity = ((ActivityUiCallable<?>) callable).getActivity();
							activity.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									try {
										Thing result = callable.call();
										addResult(result);
										latch.countDown();
									}
									catch (Exception e) {
										Log.e("JobManager.JobControlLoop", "error calling callable job in UI thread", e);
									}
								}
							});
						}
						else if (callable instanceof ControlCallable<?>) {
							jobLoopsNeeded++; // just in case, but a bit naive
							((ControlCallable<Thing>) callable).setJobQueue(callableQueue);
							parent.controlExecutor.submit(new Callable<Thing>() {
								@Override
								public Thing call() throws Exception {
									Thing result = callable.call();
									addResult(result);
									latch.countDown();
									return result;
								}
							});
						}
						else {
							jobLoopsNeeded++;
							callableQueue.add(new Callable<Thing>() {
								@Override
								public Thing call() throws Exception {
									Thing result = callable.call();
									addResult(result);
									latch.countDown();
									return result;
								}
							});
						}
					}
					
					startJobThreads(jobLoopsNeeded);
					
					latch.await();
				}
				catch (InterruptedException e) {
					Log.e("JobManager.JobControlLoop", "UI Job was interrupted.", e);
				}
				
				ArrayList<Thing> resultCopy = new ArrayList<Thing>(currentResults);
				currentResults.clear();
				parent.addResult(resultCopy);
			}
			
			parent.executeJobsComplete();
		}
		
		private void startJobThreads(int threadCount) {
			try {
				for (int i = 0; i < threadCount; i++) {
					parent.jobExecutor.submit(new JobLoop<Thing>(this, callableQueue));
				}
			}
			catch (RejectedExecutionException e) {
				// Pretty normal occurrence. The ExecutorService is letting us 
				// know we've already used up all the threads in the pool. As
				// long as there's a single thread out there, we should be ok.
			}
		}
		
		private synchronized void addResult(Thing result) {
			currentResults.add(result);
		}
	}

	private static class JobGroup<Thing> {
    	private int runCount;
    	private Collection<Callable<Thing>> callables;
    	
    	public JobGroup(Callable<Thing> callable) {
    		this.runCount = 1;
    		this.callables = new ArrayList<Callable<Thing>>();
    		this.callables.add(callable);
    	}
    	
    	public JobGroup(Callable<Thing>... callables) {
    		this.runCount = callables.length;
    		this.callables = new ArrayList<Callable<Thing>>();
    		Collections.addAll(this.callables, callables);
    	}
    	
    	public JobGroup(Runnable... runnables) {
    		this.runCount = runnables.length;
    		this.callables = new ArrayList<Callable<Thing>>();
    		
    		for (Runnable runnable : runnables) {
    			this.callables.add(Executors.callable(runnable, (Thing) null));
    		}
    	}
    	
    	public JobGroup(final Runnable runnable, final Activity activity) {
    		this(new ActivityUiCallable<Thing>() {
    			@Override
    			public Activity getActivity() {
    				return activity;
    			}
    			
    			@Override
    			public Thing call() {
    				runnable.run();
    				return null;
    			}
			});
    	}
    	
    	public CountDownLatch getCountDownLatch() {
    		return new CountDownLatch(runCount);
    	}
    	
    	public Collection<Callable<Thing>> getCallables() {
    		return Collections.unmodifiableCollection(callables);
    	}
    }

	private final Queue<JobManager.JobGroup<Thing>> jobQueue;
	private final Queue<List<Thing>> results;
	private final ExecutorService controlExecutor;
	private final ExecutorService jobExecutor;

	public JobManager(ExecutorService controlExecutor, ExecutorService jobExecutor) {
		this.controlExecutor = controlExecutor;
		this.jobExecutor = jobExecutor;
		
		jobQueue = new LinkedList<JobManager.JobGroup<Thing>>();
		results = new LinkedList<List<Thing>>();
	}
	
	public void submit(Callable<Thing> callable) {
		jobQueue.offer(new JobManager.JobGroup<Thing>(callable));
	}
	
	public void submit(Callable<Thing>... callables) {
		jobQueue.offer(new JobManager.JobGroup<Thing>(callables));
	}
	
	public void submit(Runnable... runnables) {
		jobQueue.offer(new JobManager.JobGroup<Thing>(runnables));
	}
	
	public void submit(Runnable runnable, Activity activity) {
		jobQueue.offer(new JobManager.JobGroup<Thing>(runnable, activity));
	}
	
	public void executeJobs() {
		controlExecutor.submit(new JobControlLoop<Thing>(this, jobQueue));
	}
	
	protected void executeJobsStarted() { }
	
	protected void executeJobsComplete() { }
	
	public synchronized Queue<List<Thing>> getResults() {
		return results;
	}
	
	private synchronized void addResult(List<Thing> result) {
		results.offer(result);
	}
}
