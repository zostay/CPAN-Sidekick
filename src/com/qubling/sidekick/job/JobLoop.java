package com.qubling.sidekick.job;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import android.util.Log;

class JobLoop implements Runnable {
	private static final long DEFAULT_JOB_WAIT_TIMEOUT = 10L;
	private static final TimeUnit DEFAULT_JOB_WAIT_UNIT = TimeUnit.SECONDS;
	
	private static int loopIdCounter = 0;
	
	private int loopId;
	private BlockingQueue<Callable<Void>> jobQueue;
	
	private long jobWaitTimeout = DEFAULT_JOB_WAIT_TIMEOUT;
	private TimeUnit jobWaitUnit = DEFAULT_JOB_WAIT_UNIT;
	
	public JobLoop(BlockingQueue<Callable<Void>> jobQueue) {
		this.loopId = ++loopIdCounter;
		this.jobQueue = jobQueue;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Log.d("JobManager.JobLoop", this + ": Polling for job");
				Callable<Void> job = jobQueue.poll(jobWaitTimeout, jobWaitUnit);
				if (job == null) {
					Log.d("JobManager.JobLoop", this + ": Polling returned null. Quitting.");
					return;
				}
				Log.d("JobManager.JobLoop", this + ": Running job " + job);
				job.call();
			}
			catch (InterruptedException e) {
				Log.e("JobManager.JobLoop", this + ": Interrupted. Quitting.");
				return;
			}
			catch (Exception e) {
				Log.e("JobManager.JobLoop", this + ": Error running job", e);
			}
		}
	}
	
	@Override
	public String toString() {
		return "JobLoop #" + loopId;
	}
}