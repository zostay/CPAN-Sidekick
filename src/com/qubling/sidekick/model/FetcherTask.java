package com.qubling.sidekick.model;

import java.util.concurrent.FutureTask;

public class FetcherTask<SomeInstance extends Instance> extends FutureTask<ResultSet<SomeInstance>> {
	public FetcherTask(final Fetcher<SomeInstance> fetcher) {
		super(fetcher);
	}
}
