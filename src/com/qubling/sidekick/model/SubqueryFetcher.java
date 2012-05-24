package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import android.app.Activity;
import android.util.Log;

public class SubqueryFetcher<SomeInstance extends Instance<SomeInstance>, ForeignInstance extends Instance<ForeignInstance>> 
	extends AbstractFetcher<SomeInstance> implements UpdateFetcher<SomeInstance> {
	
	private Results.Remap<SomeInstance, ForeignInstance> remapper;
	private UpdateFetcher<ForeignInstance> fetcher;
	
	public SubqueryFetcher(Model<SomeInstance> model, UpdateFetcher<ForeignInstance> fetcher, Results.Remap<SomeInstance, ForeignInstance> remapper) {
		super(model);
		
		this.fetcher = fetcher;
		this.remapper = remapper;
	}
	
	@Override
	public boolean needsUpdate(SomeInstance instance) {
		Collection<ForeignInstance> others = remapper.map(instance);
		for (ForeignInstance other : others) {
			if (fetcher.needsUpdate(other))
				return true;
		}
		
		return false;
	}
	
	@Override
	public void setIncomingResultSet(ResultsForUpdate<SomeInstance> inputResults) {
		setResultSet(inputResults);
	}
	
	@Override
	public ExecutorService getPreferredExecutor() {
		return getSchema().getControlExecutor();
	}
	
	@Override
	protected ResultSet<SomeInstance> execute() throws Exception {
		Log.d("SubqueryFetcher", "START execute()");
		
		CountDownLatch latch = new CountDownLatch(1);
		ExecutorService service = getSchema().getJobExecutor();
		
		ResultSet<ForeignInstance> inputResults = new Results<ForeignInstance>();
		inputResults.addRemap(getResultSet(), remapper);
		fetcher.setIncomingResultSet(
				new ResultsForUpdate<ForeignInstance>(fetcher, inputResults));
		
		service.submit(JobManager.countDownCallable(latch, fetcher));
		latch.await();
		
		Log.d("SubqueryFetcher", "END execute()");
		
		return getResultSet();
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
