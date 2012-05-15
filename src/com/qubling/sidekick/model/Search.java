package com.qubling.sidekick.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Activity;
import android.util.Log;

public class Search<SomeInstance extends Instance<SomeInstance>> {
	public interface OnFinishedFetch<SomeInstance extends Instance<SomeInstance>> {
		public abstract void onFinishedFetch(List<ResultSet<SomeInstance>> results);
	}
	
	private static class Job<Thing> implements Runnable {
		private int runCount;
		private ExecutorService jobExecutor;
		private Collection<Callable<Thing>> callables;
		private Activity activity;
		private List<Future<Thing>> results;
		
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
					results = new ArrayList<Future<Thing>>();
					if (callable instanceof Fetcher<?>) {
						Fetcher<?> fetcher = (Fetcher<?>) callable;
						results.add(fetcher.getPreferredExecutor().submit(callable));
					}
					else {
						results.add(jobExecutor.submit(callable));
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
		
		public List<Thing> getResults() {
			if (results == null) return null;
			
			try {
				List<Thing> things = new ArrayList<Thing>();
				for (Future<Thing> futureThing : results) {
					things.add(futureThing.get());
				}
				return things;
			}
			catch (ExecutionException e) {
				Log.e("Search", "Execution failed getting results.", e);
				return null;
			}
			catch (InterruptedException e) {
				Log.e("Search", "Execution interrupted getting results.", e);
				return null;
			}
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
	private final Deque<List<ResultSet<SomeInstance>>> results;
	private final ExecutorService controlExecutor;
	private final ExecutorService jobExecutor;
	
	public Search(ExecutorService controlExecutor, ExecutorService jobExecutor, Fetcher<SomeInstance>... fetchers) {
		this.controlExecutor = controlExecutor;
		this.jobExecutor = jobExecutor;
		
		jobQueue = new ArrayDeque<Job<ResultSet<SomeInstance>>>();
		jobQueue.offer(new Job<ResultSet<SomeInstance>>(fetchers.length, jobExecutor, fetchers));
		
		results = new ArrayDeque<List<ResultSet<SomeInstance>>>();
	}
	
	public Search<SomeInstance> thenDoFetch(Fetcher<SomeInstance>... fetchers) {
		jobQueue.offer(new Job<ResultSet<SomeInstance>>(fetchers.length, jobExecutor, fetchers));
		return this;
	}
	
	public Search<SomeInstance> whenFinishedRun(Runnable... runnables) {
		jobQueue.offer(new Job<ResultSet<SomeInstance>>(runnables.length, jobExecutor, runnables));
		return this;
	}
	
	public Search<SomeInstance> whenFinishedRunInUiThread(Activity activity, Runnable runnable) {
		jobQueue.offer(new Job<ResultSet<SomeInstance>>(1, jobExecutor, runnable, activity));
		return this;
	}
	
	public Search<SomeInstance> whenFinishedNotify(final OnFinishedFetch<SomeInstance> listener) {
		Runnable notifier = new Runnable() {
			
			@Override
			public void run() {
				listener.onFinishedFetch(results.peek());
			}
		};
		
		jobQueue.offer(new Job<ResultSet<SomeInstance>>(1, jobExecutor, notifier));
		return this;
	}
	
	public Search<SomeInstance> whenFinishedNotifyUi(Activity activity, final OnFinishedFetch<SomeInstance> listener) {
		Runnable notifier = new Runnable() {
			
			@Override
			public void run() {
				listener.onFinishedFetch(results.peek());
			}
		};
		
		jobQueue.offer(new Job<ResultSet<SomeInstance>>(1, jobExecutor, notifier, activity));
		return this;
	}
	
	public Search<SomeInstance> start() {
		controlExecutor.submit(new Runnable() {
			@Override
			public void run() {
				while (!jobQueue.isEmpty()) {
					final Job<ResultSet<SomeInstance>> nextJob = jobQueue.poll();
					
					if (nextJob.getActivity() != null) {
						final CountDownLatch uiLatch = new CountDownLatch(1);
						nextJob.getActivity().runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								nextJob.run();
								uiLatch.countDown();
							}
						});
						results.push(nextJob.getResults());
					}
					else {
						nextJob.run();
						results.push(nextJob.getResults());
					}
				}
			}
		});
		
		return this;
	}
}
