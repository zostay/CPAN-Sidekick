package com.qubling.sidekick.job;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;


import android.util.Log;


class JobControlLoop implements Runnable {
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
			
			Log.d("JobManager.JobControlLoop", "Start " + nextJob);
			
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