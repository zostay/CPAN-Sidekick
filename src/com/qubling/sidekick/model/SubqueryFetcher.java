package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import android.app.Activity;
import android.util.Log;

public class SubqueryFetcher<SomeInstance extends Instance<SomeInstance>, ForeignInstance extends Instance<ForeignInstance>> 
	extends AbstractFetcher<SomeInstance> 
	implements UpdateFetcher<SomeInstance>, ControlCallable<ResultSet<SomeInstance>> {
	
	private Results.Remap<SomeInstance, ForeignInstance> remapper;
	private UpdateFetcher<ForeignInstance> fetcher;
	private BlockingQueue<Callable<ResultSet<SomeInstance>>> jobQueue;
	
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
    public void setJobQueue(BlockingQueue<Callable<ResultSet<SomeInstance>>> jobQueue) {
		Log.d("SubqueryFetcher", "setJobQueue()");
	    this.jobQueue = jobQueue;
    }

	@Override
	protected ResultSet<SomeInstance> execute() throws Exception {
		Log.d("SubqueryFetcher", "START execute()");
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		ResultSet<ForeignInstance> inputResults = new Results<ForeignInstance>();
		inputResults.addRemap(getResultSet(), remapper);
		fetcher.setIncomingResultSet(
				new ResultsForUpdate<ForeignInstance>(fetcher, inputResults));

		jobQueue.add(new Callable<ResultSet<SomeInstance>>() {
			@Override
			public ResultSet<SomeInstance> call() throws Exception {
				fetcher.call();
				latch.countDown();
				return SubqueryFetcher.this.getResultSet();
			}
		});
		
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
