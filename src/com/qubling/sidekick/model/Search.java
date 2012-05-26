package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.HashSet;

import android.app.Activity;
import android.util.Log;

public class Search<SomeInstance extends Instance<SomeInstance>> implements JobManager.Monitor {
	public interface OnFinishedFetch<SomeInstance extends Instance<SomeInstance>> {
		public void onFinishedFetch(ResultSet<SomeInstance> results);
	}
	
	public interface OnSearchActivity {
		public void onSearchStart();
		public void onSearchComplete();
	}
	
	private final JobManager jobManager;
	private final Fetcher<SomeInstance> originalFetcher;
	private final Collection<OnSearchActivity> activityListeners;
	
	public Search(JobManager jobManager, Fetcher<SomeInstance> fetcher) {
		this.jobManager = jobManager;
		this.jobManager.setJobManagerMonitor(this);
		this.jobManager.submit(fetcher);

		this.originalFetcher = fetcher;
		this.activityListeners = new HashSet<Search.OnSearchActivity>();
	}
	
	public Search<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance>... fetchers) {
		for (UpdateFetcher<SomeInstance> fetcher : fetchers) {
			Log.d("Search", "originalFetcher.getResultSet() " + originalFetcher + " " + originalFetcher.getResultSet());
			fetcher.setIncomingResultSet(
					new ResultsForUpdate<SomeInstance>(fetcher, originalFetcher.getResultSet()));
		}
		
		jobManager.submit(fetchers);
		return this;
	}
	
	public Search<SomeInstance> whenFinishedRun(Runnable... runnables) {
		jobManager.submit(runnables);
		return this;
	}
	
	public Search<SomeInstance> whenFinishedRunInUiThread(Activity activity, Runnable runnable) {
		jobManager.submit(runnable, activity);
		return this;
	}
	
	public Search<SomeInstance> whenFinishedNotify(final OnFinishedFetch<SomeInstance> listener) {
		Runnable notifier = new Runnable() {
			
			@Override
			public void run() {
				listener.onFinishedFetch(originalFetcher.getResultSet());
			}
		};
		
		jobManager.submit(notifier);
		return this;
	}
	
	public Search<SomeInstance> whenFinishedNotifyUi(Activity activity, final OnFinishedFetch<SomeInstance> listener) {
		Runnable notifier = new Runnable() {
			
			@Override
			public void run() {
				listener.onFinishedFetch(originalFetcher.getResultSet());
			}
		};
		
		jobManager.submit(notifier, activity);
		return this;
	}
	
	public Search<SomeInstance> start() {
		jobManager.executeJobs();
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
	public void executeJobsStarted() {
		Log.d("Search", "notifyOnSearchStart()");
		for (OnSearchActivity listener : activityListeners) {
			listener.onSearchStart();
		}
	}
	
	@Override
	public void executeJobsComplete() {
		Log.d("Search", "notifyOnSearchComplete()");
		for (OnSearchActivity listener : activityListeners) {
			listener.onSearchComplete();
		}
	}
}
