package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class SubqueryFetcher<SomeInstance extends Instance, ForeignInstance extends Instance> extends Fetcher<SomeInstance> implements Fetcher.OnComplete<ForeignInstance> {
	
	public interface ReturnMap<ForeignInstance extends Instance, SomeInstance extends Instance> {
		public void applyMap(ResultSet<ForeignInstance> fromResults, ResultSet<SomeInstance> toResults);
	}

	private Collection<Fetcher<ForeignInstance>> fetchers;
	private ReturnMap<ForeignInstance, SomeInstance> returnMap;
	
	public SubqueryFetcher(Fetcher<ForeignInstance> fetcher, ReturnMap<ForeignInstance, SomeInstance> returnMap) {
		this.fetchers = Collections.singleton(fetcher);
		this.returnMap = returnMap;
	}
	
	public SubqueryFetcher(Collection<Fetcher<ForeignInstance>> fetchers, ReturnMap<ForeignInstance, SomeInstance> returnMap) {
		this.fetchers = fetchers;
		this.returnMap = returnMap;
	}
	
	@Override
	public ExecutorService getPreferredExecutor() {
		return getSchema().getControlExecutor();
	}
	
	@Override
	protected ResultSet<SomeInstance> execute() throws Exception {
		CountDownLatch latch = new CountDownLatch(fetchers.size());
		ExecutorService service = getSchema().getJobExecutor();
		service.invokeAll(Search.countDownCallables(latch, fetchers));
		latch.await();
		
		return getResultSet();
	}

	@Override
    public void onComplete(Fetcher<ForeignInstance> fetcher, ResultSet<ForeignInstance> results) {
		fetcher.removeOnCompleteListener(this);
		if (returnMap != null)
			returnMap.applyMap(results, getResultSet());
    }
}
