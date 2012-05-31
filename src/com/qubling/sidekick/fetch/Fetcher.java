package com.qubling.sidekick.fetch;

import com.qubling.sidekick.model.Instance;
import com.qubling.sidekick.search.ResultSet;

public interface Fetcher<SomeInstance extends Instance<SomeInstance>> extends Runnable {
	public interface OnFinished<SomeInstance extends Instance<SomeInstance>> {
    	public abstract void onFinishedFetch(Fetcher<SomeInstance> fetcher, ResultSet<SomeInstance> results);
    }

	public ResultSet<SomeInstance> getResultSet();
}
