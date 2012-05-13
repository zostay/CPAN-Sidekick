package com.qubling.sidekick.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.util.Log;

public class Search<SomeInstance extends Instance> {
	public interface OnComplete<SomeInstance extends Instance> {
		public abstract void onComplete(Search<SomeInstance> search, ResultSet<SomeInstance> results);
	}
	
	private static class Job<Thing> implements Runnable {
		private int runCount;
		private ExecutorService jobExecutor;
		private Collection<Callable<Thing>> callables;
		private Activity activity;
		
		public Job(int runCount, ExecutorService jobExecutor, Callable<Thing>... callables) {
			this.runCount = runCount;
			this.jobExecutor = jobExecutor;
			this.callables = new ArrayList<Callable<Thing>>();
			Collections.addAll(this.callables, callables);
		}
		
		public Job(int runCount, ExecutorService jobExecutor, Runnable... runnables) {
			this.runCount = runCount;
			this.jobExecutor = jobExecutor;
			this.callables = new ArrayList<Callable<Thing>>();
			
			for (Runnable runnable : runnables) {
				this.callables.add(Executors.callable(runnable, (Thing) null));
			}
		}
		
		public Job(int runCount, ExecutorService jobExecutor, Runnable runnable, Activity activity) {
			this(runCount, jobExecutor, runnable);
			this.activity = activity;
		}
		
		@Override
		public void run() {
			try {
				CountDownLatch latch = new CountDownLatch(runCount);
				Search.countDownCallables(latch, callables);
				for (Callable<Thing> callable : callables) {
					if (callable instanceof Fetcher<?>) {
						Fetcher<?> fetcher = (Fetcher<?>) callable;
						fetcher.getPreferredExecutor().submit(callable);
					}
					else {
						jobExecutor.submit(callable);
					}
				}
				latch.await();
			}
			catch (InterruptedException e) {
				Log.e("Search.Job", "Interrupted while running a search job.", e);
			}
		}

		public Activity getActivity() {
			return activity;
		}
	}
	
	public static <Thing> Collection<Callable<Thing>> countDownCallables(final CountDownLatch latch, final Collection<? extends Callable<Thing>> plainCallables) {
		ArrayList<Callable<Thing>> latchedCallables = new ArrayList<Callable<Thing>>();
		for (final Callable<Thing> callable : plainCallables) {
			latchedCallables.add(new Callable<Thing>() {
				@Override
				public Thing call() throws Exception {
					Thing results = callable.call();
					latch.countDown();
					return results;
				}
			});
		}
		
		return latchedCallables;
	}
	
	private final Deque<Job<ResultSet<SomeInstance>>> jobQueue;
	private final ExecutorService controlExecutor;
	private final ExecutorService jobExecutor;
	
	public Search(ExecutorService controlExecutor, ExecutorService jobExecutor, Fetcher<SomeInstance>... fetchers) {
		this.controlExecutor = controlExecutor;
		this.jobExecutor = jobExecutor;
		
		jobQueue = new ArrayDeque<Job<ResultSet<SomeInstance>>>();
		jobQueue.offer(new Job<ResultSet<SomeInstance>>(fetchers.length, jobExecutor, fetchers));
	}
	
	public Search<SomeInstance> thenDoSearch(Fetcher<SomeInstance>... fetchers) {
		jobQueue.offer(new Job<ResultSet<SomeInstance>>(fetchers.length, jobExecutor, fetchers));
		return this;
	}
	
	public Search<SomeInstance> whenComplete(Runnable... runnables) {
		jobQueue.offer(new Job<ResultSet<SomeInstance>>(runnables.length, jobExecutor, runnables));
		return this;
	}
	
	public Search<SomeInstance> whenCompleteRunInUiThread(Activity activity, Runnable runnable) {
		jobQueue.offer(new Job<ResultSet<SomeInstance>>(1, jobExecutor, runnable, activity));
		return this;
	}
	
	public Search<SomeInstance> start() {
		controlExecutor.submit(new Runnable() {
			@Override
			public void run() {
				while (!jobQueue.isEmpty()) {
					Job<ResultSet<SomeInstance>> nextFetcherSequence = jobQueue.poll();
					
					if (nextFetcherSequence.getActivity() != null) {
						nextFetcherSequence.getActivity().runOnUiThread(nextFetcherSequence);
					}
					else {
						jobExecutor.submit(nextFetcherSequence);
					}
				}
			}
		});
		
		return this;
	}
}
