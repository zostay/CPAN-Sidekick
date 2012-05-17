package com.qubling.sidekick.model;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import android.app.Activity;

public interface Fetcher<SomeInstance extends Instance<SomeInstance>> extends Callable<ResultSet<SomeInstance>> {
	public interface OnFinished<SomeInstance extends Instance<SomeInstance>> {
    	public abstract void onFinishedFetch(Fetcher<SomeInstance> fetcher, ResultSet<SomeInstance> results);
    }

	public ResultSet<SomeInstance> getResultSet();
	
	public abstract Fetcher<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance> fetcher);

	public abstract Fetcher<SomeInstance> whenFinishedNotifyUi(Activity activity, OnFinished<SomeInstance> listener);

	public abstract Fetcher<SomeInstance> whenFinishedNotify(OnFinished<SomeInstance> listener);

	public ExecutorService getPreferredExecutor();
}
