package com.qubling.sidekick.model;

import android.app.Activity;

public abstract class CPANQueryUpdateFetcher<SomeInstance extends Instance<SomeInstance>>
        extends CPANQueryFetcher<SomeInstance> implements
        UpdateFetcher<SomeInstance> {
	
	public CPANQueryUpdateFetcher(Model<SomeInstance> model, SearchSection searchSection, String searchTemplate) {
		super(model, searchSection, searchTemplate);
	}

	@Override
	public void setIncomingResultSet(ResultsForUpdate<SomeInstance> input) {
		setResultSet(input);
	}

	@Override
    public UpdateFetcher<SomeInstance> thenAfterUpdateDoFetch(UpdateFetcher<SomeInstance> fetcher) {
	    thenDoFetch(fetcher);
	    return this;
    }

	@Override
    public UpdateFetcher<SomeInstance> whenUpdateFinishedNotifyUi(Activity activity, OnFinished<SomeInstance> listener) {
	    whenFinishedNotifyUi(activity, listener);
	    return this;
    }

	@Override
    public UpdateFetcher<SomeInstance> whenUpdateFinishedNotify(OnFinished<SomeInstance> listener) {
	    whenFinishedNotify(listener);
	    return this;
    }

}
