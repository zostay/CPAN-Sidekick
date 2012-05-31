package com.qubling.sidekick.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.qubling.sidekick.job.JobExecutor;
import com.qubling.sidekick.job.JobMonitor;
import com.qubling.sidekick.job.Job;

import android.app.Activity;
import android.util.Log;

public class Search<SomeInstance extends Instance<SomeInstance>> 
	implements JobMonitor {
	
	public interface OnSearchActivity {
		public void onSearchStart();
		public void onSearchComplete();
	}
	
	private static class Plan extends ArrayList<Job> {
        private static final long serialVersionUID = -8139612794037092258L; 
	}
	
	private final Activity activity;
	private final Fetcher<SomeInstance> originalFetcher;
	private final Search.Plan searchPlan;
	private final Fetcher.OnFinished<SomeInstance> finishListener;
	private final Collection<OnSearchActivity> activityListeners;
	
	private Runnable makeFollowup(final Fetcher<SomeInstance> fetcher) {
		if (finishListener == null) return null;
		
		return new Runnable() {
			@Override
			public void run() {
				finishListener.onFinishedFetch(fetcher, originalFetcher.getResultSet());
			}
			
			@Override
			public String toString() {
				return "Followup " + finishListener;
			}
		};
	}
	
	public Search(Activity activity, Fetcher<SomeInstance> fetcher, Fetcher.OnFinished<SomeInstance> listener) {
		finishListener = listener;
		
		this.activity = activity;
		
		this.searchPlan = new Search.Plan();
		Job originalJob = new Job(activity);
		originalJob.addCommand(fetcher, makeFollowup(fetcher));
		this.searchPlan.add(originalJob);
		
		this.originalFetcher = fetcher;
		this.activityListeners = new HashSet<Search.OnSearchActivity>();
	}
	
	public Search<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance>... fetchersArray) {
		Job fetcherJob = new Job(activity);
		for (UpdateFetcher<SomeInstance> fetcher : fetchersArray) {
			Log.d("Search", "originalFetcher.getResultSet() " + originalFetcher + " " + originalFetcher.getResultSet());
			fetcher.setIncomingResultSet(
					new ResultsForUpdate<SomeInstance>(fetcher, originalFetcher.getResultSet()));
			
			fetcherJob.addCommand(fetcher, makeFollowup(fetcher));
		}
		
		searchPlan.add(fetcherJob);
		
		return this;
	}
	
	public Search<SomeInstance> whenFinishedRun(Runnable... runnablesArray) {
		Job commandJob = new Job(activity);
		for (Runnable runnable : runnablesArray) {
			commandJob.addCommand(runnable);
		}
		
		searchPlan.add(commandJob);
		
		return this;
	}
	
	public Search<SomeInstance> start() {
		executeJobsStarted();
		
		JobExecutor executor = new JobExecutor(activity);
		for (Job job : searchPlan) {
			executor.addCommand(job);
		}
		
		Runnable[] finalCommands = new Runnable[1];
		finalCommands[0] = new Runnable() {
			@Override
			public void run() {
				executeJobsComplete();
			}
			
			@Override
			public String toString() {
				return "Notify executeJobsComplete()";
			}
		};
		executor.execute(finalCommands);
		
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
