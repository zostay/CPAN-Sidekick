package com.qubling.sidekick.model;

import java.util.concurrent.Callable;

import android.app.Activity;

public interface Fetcher<SomeInstance extends Instance<SomeInstance>> extends Callable<Void> {
	public interface OnFinished<SomeInstance extends Instance<SomeInstance>> {
    	public abstract void onFinishedFetch(Fetcher<SomeInstance> fetcher, ResultSet<SomeInstance> results);
    }

	public ResultSet<SomeInstance> getResultSet();
	
	public abstract Consequence<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance> fetcher);

	public abstract Consequence<SomeInstance> whenFinishedNotifyUi(Activity activity, OnFinished<SomeInstance> listener);

	public abstract Consequence<SomeInstance> whenFinishedNotify(OnFinished<SomeInstance> listener);
}
