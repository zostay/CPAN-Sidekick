package com.qubling.sidekick.model;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class SubqueryFetcher<SomeInstance extends Instance<SomeInstance>, ForeignInstance extends Instance<ForeignInstance>> 
	extends AbstractFetcher<SomeInstance> implements UpdateFetcher<SomeInstance> {
	
	private ResultSet.Remap<SomeInstance, ForeignInstance> remapper;
	private UpdateFetcher<ForeignInstance> fetcher;
	
	public SubqueryFetcher(Model<SomeInstance> model, UpdateFetcher<ForeignInstance> fetcher, ResultSet.Remap<SomeInstance, ForeignInstance> remapper) {
		super(model);
		
		this.fetcher = fetcher;
		this.remapper = remapper;
	}
	
	@Override
	public void setIncomingResultSet(ResultSet<SomeInstance> inputResults) {
		setResultSet(inputResults);
	}
	
	@Override
	public ExecutorService getPreferredExecutor() {
		return getSchema().getControlExecutor();
	}
	
	@Override
	protected ResultSet<SomeInstance> execute() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		ExecutorService service = getSchema().getJobExecutor();
		
		ResultSet<ForeignInstance> inputResults = new ResultSet<ForeignInstance>();
		inputResults.addRemap(getResultSet(), remapper);
		fetcher.setIncomingResultSet(inputResults);
		
		service.submit(Search.countDownCallable(latch, fetcher));
		latch.await();
		
		return getResultSet();
	}
}
