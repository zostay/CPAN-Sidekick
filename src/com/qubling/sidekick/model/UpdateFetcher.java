package com.qubling.sidekick.model;

import android.app.Activity;

public interface UpdateFetcher<SomeInstance extends Instance<SomeInstance>> extends Fetcher<SomeInstance> {
	public void setIncomingResultSet(ResultsForUpdate<SomeInstance> input);
	
	public boolean needsUpdate(SomeInstance instance);
	
	public abstract UpdateFetcher<SomeInstance> thenAfterUpdateDoFetch(UpdateFetcher<SomeInstance> fetcher);

	public abstract UpdateFetcher<SomeInstance> whenUpdateFinishedNotifyUi(Activity activity, OnFinished<SomeInstance> listener);

	public abstract UpdateFetcher<SomeInstance> whenUpdateFinishedNotify(OnFinished<SomeInstance> listener);
}
