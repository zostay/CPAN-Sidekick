package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;

import android.app.Activity;
import android.util.Log;

public class Search<SomeInstance extends Instance<SomeInstance>> extends JobManager<ResultSet<SomeInstance>> {
	public interface OnFinishedFetch<SomeInstance extends Instance<SomeInstance>> {
		public void onFinishedFetch(List<ResultSet<SomeInstance>> results);
	}
	
	public interface OnSearchActivity {
		public void onSearchStart();
		public void onSearchComplete();
	}
	
	private final Fetcher<SomeInstance> originalFetcher;
	private final Collection<OnSearchActivity> activityListeners;
	
	public Search(ExecutorService controlExecutor, ExecutorService jobExecutor, Fetcher<SomeInstance> fetcher) {
		super(controlExecutor, jobExecutor);
		
		submit(fetcher);

		this.originalFetcher = fetcher;
		this.activityListeners = new HashSet<Search.OnSearchActivity>();
	}
	
	public Search<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance>... fetchers) {
		for (UpdateFetcher<SomeInstance> fetcher : fetchers) {
			Log.d("Search", "originalFetcher.getResultSet() " + originalFetcher + " " + originalFetcher.getResultSet());
			fetcher.setIncomingResultSet(
					new ResultsForUpdate<SomeInstance>(fetcher, originalFetcher.getResultSet()));
		}
		
		submit(fetchers);
		return this;
	}
	
	public Search<SomeInstance> whenFinishedRun(Runnable... runnables) {
		submit(runnables);
		return this;
	}
	
	public Search<SomeInstance> whenFinishedRunInUiThread(Activity activity, Runnable runnable) {
		submit(runnable, activity);
		return this;
	}
	
	public Search<SomeInstance> whenFinishedNotify(final OnFinishedFetch<SomeInstance> listener) {
		Runnable notifier = new Runnable() {
			
			@Override
			public void run() {
				listener.onFinishedFetch(getResults().peek());
			}
		};
		
		submit(notifier);
		return this;
	}
	
	public Search<SomeInstance> whenFinishedNotifyUi(Activity activity, final OnFinishedFetch<SomeInstance> listener) {
		Runnable notifier = new Runnable() {
			
			@Override
			public void run() {
				listener.onFinishedFetch(getResults().peek());
			}
		};
		
		submit(notifier, activity);
		return this;
	}
	
	public Search<SomeInstance> start() {
		executeJobs();
		return this;
	}
	
	public Search<SomeInstance> fetchMore() {
		if (originalFetcher instanceof LimitedFetcher<?>) {
			LimitedFetcher<?> limitFetcher = (LimitedFetcher<?>) originalFetcher;
			ResultSet<SomeInstance> results = originalFetcher.getResultSet();
			if (results.getTotalSize() > limitFetcher.getSize()) {
				limitFetcher.setFrom(limitFetcher.getSize());
			}
		}
		
		return start();
	}
	
	public ResultSet<SomeInstance> getResultSet() {
		return originalFetcher.getResultSet();
	}
	
	public void addOnSearchActivityListener(OnSearchActivity listener) {
		activityListeners.add(listener);
	}
	
	public void removeOnSearchActivityListener(OnSearchActivity listener) {
		activityListeners.remove(listener);
	}
	
	@Override
	protected void executeJobsStarted() {
		Log.d("Search", "notifyOnSearchStart()");
		for (OnSearchActivity listener : activityListeners) {
			listener.onSearchStart();
		}
	}
	
	@Override
	protected void executeJobsComplete() {
		Log.d("Search", "notifyOnSearchComplete()");
		for (OnSearchActivity listener : activityListeners) {
			listener.onSearchComplete();
		}
	}
}
