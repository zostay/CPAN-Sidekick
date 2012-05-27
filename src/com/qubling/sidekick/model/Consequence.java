package com.qubling.sidekick.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import com.qubling.sidekick.job.ControlCallable;
import com.qubling.sidekick.job.JobManager;

import android.util.Log;

public class Consequence<SomeInstance extends Instance<SomeInstance>>
        extends AbstractFetcher<SomeInstance>
        implements UpdateFetcher<SomeInstance>, ControlCallable<Void> {
	
	private JobManager jobManager;
	private Fetcher<SomeInstance> fetcher;
	private List<Callable<Void>> consequences;

	public Consequence(Model<SomeInstance> model, Fetcher<SomeInstance> fetcher) {
	    super(model);
	    
	    this.fetcher = fetcher;
	    this.consequences = new ArrayList<Callable<Void>>();
	    
	    Log.d("Consequence", "Constructing " +  this);
    }

	@Override
	protected void execute() throws Exception {
		CountDownLatch latch = new CountDownLatch(1);
		Log.d("Consequence", "Run " + fetcher);
		jobManager.addToJobQueue(fetcher, latch);
		for (Callable<Void> consequence : consequences) {
			latch.await();
			
			latch = new CountDownLatch(1);
			Log.d("Consequence", "Then Run " + consequence);
			jobManager.addToJobQueue(consequence, latch);
		}
		latch.await();
	}

	@Override
    public void setJobManager(JobManager jobManager) {
	    this.jobManager = jobManager;
    }
	
	public Consequence<SomeInstance> addConsequence(Callable<Void> consequence) {
		consequences.add(consequence);
		return this;
	}

	@Override
    public void setIncomingResultSet(ResultsForUpdate<SomeInstance> input) {
		((UpdateFetcher<SomeInstance> ) fetcher).setIncomingResultSet(input);
    }

	@Override
    public boolean needsUpdate(SomeInstance instance) {
	    return ((UpdateFetcher<SomeInstance>) fetcher).needsUpdate(instance);
    }

	@Override
	public String toString() {
		StringBuilder consequencesList = new StringBuilder();
		for (int i = 0; i < consequences.size() && i < 3; i++) {
			consequencesList.append(consequences.get(i));
			consequencesList.append(",");
		}
		
		if (consequences.size() > 3) {
			consequencesList.append("...");
		}
		
		return getModel() + ":Consequence(" + fetcher + ";" + consequencesList + ")";
	}
}
