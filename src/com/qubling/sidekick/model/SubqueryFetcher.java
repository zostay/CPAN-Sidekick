package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import com.qubling.sidekick.job.ControlCallable;
import com.qubling.sidekick.job.JobManager;

import android.util.Log;

public class SubqueryFetcher<SomeInstance extends Instance<SomeInstance>, ForeignInstance extends Instance<ForeignInstance>> 
	extends AbstractFetcher<SomeInstance> 
	implements UpdateFetcher<SomeInstance>, ControlCallable<Void> {
	
	private Results.Remap<SomeInstance, ForeignInstance> remapper;
	private UpdateFetcher<ForeignInstance> fetcher;
	private JobManager jobManager;
	
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
    public void setJobManager(JobManager jobManager) {
		Log.d("SubqueryFetcher", "setJobManager(" + jobManager + ")");
	    this.jobManager = jobManager;
    }

	@Override
	protected void execute() throws Exception {
		Log.d("SubqueryFetcher", "START execute()");
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		ResultSet<ForeignInstance> inputResults = new Results<ForeignInstance>();
		inputResults.addRemap(getResultSet(), remapper);
		fetcher.setIncomingResultSet(
				new ResultsForUpdate<ForeignInstance>(fetcher, inputResults));

		Log.d("SubqueryFetcher", this + " jobManager " + fetcher + " " + jobManager);
		
		jobManager.addToJobQueue(fetcher, latch);
		
		latch.await();
		
		Log.d("SubqueryFetcher", "END execute()");
	}
	
	@Override
	public String toString() {
		return getModel() + ":SubqueryFetcher(" + fetcher + ";" + getResultSet() + ")";
	}
}
