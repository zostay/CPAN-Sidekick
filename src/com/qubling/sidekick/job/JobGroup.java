package com.qubling.sidekick.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;


import android.app.Activity;

class JobGroup {
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
			
			@Override
			public String toString() {
				return "JobGroup:ActivityUiCallable(" + runnable + ")";
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