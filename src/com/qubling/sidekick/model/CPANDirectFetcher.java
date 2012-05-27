package com.qubling.sidekick.model;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.qubling.sidekick.job.ControlCallable;
import com.qubling.sidekick.job.JobManager;

import android.util.Log;

public abstract class CPANDirectFetcher<SomeInstance extends Instance<SomeInstance>> 
	extends CPANFetcher<SomeInstance> 
	implements UpdateFetcher<SomeInstance>, ControlCallable<Void> {
	
	/**
	 * An enumeration of direct retrieval URLs on MetaCPAN.
	 *
	 * @author sterling
	 *
	 */
    public enum FetchSection {
    	MODULE_POD(METACPAN_API_POD_URL),
    	MODULE_FETCH(METACPAN_API_MODULE_URL);

        private String baseUrl;

        FetchSection(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getBaseUrl() {
            return baseUrl;
        }
    }
    
    private FetchSection fetchSection;
    private JobManager jobManager;
    
    public CPANDirectFetcher(Model<SomeInstance> model, FetchSection fetchSection) {
    	super(model);
    	
    	this.fetchSection  = fetchSection;
    }
	
	@Override
    public void setJobManager(JobManager jobManager) {
	    this.jobManager = jobManager;
    }

	@Override
	protected void execute() throws IOException, InterruptedException {
		Log.d("CPANDirectFetcher", "START execute()");
		
		final CountDownLatch latch = new CountDownLatch(getResultSet().size());
		
		for (final SomeInstance instance : getResultSet()) {
			Callable<Void> job = new Callable<Void>() {
				@Override
				public Void call() throws IOException {
					CPANDirectFetcher.this.fetchOne(instance);
					return null;
				}
			};
			
			jobManager.addToJobQueue(job, latch);
		}
		
		latch.await();
		
		Log.d("CPANDirectFetcher", "END execute()");
	}
	
    protected void fetchOne(SomeInstance instance) throws IOException {
        String fetchContent;
        try {
            HttpGet fetchRequest = new HttpGet(fetchSection.getBaseUrl() + instance.getKey());
            HttpResponse fetchResponse = getHttpClient().execute(fetchRequest);

            fetchContent = slurpContent(fetchResponse);
        }

        catch (IOException e) {
            Log.e("CPANDirectFetcher", "Cannot fetch from " + fetchSection.getBaseUrl() + instance.getKey() + ": " + e.getMessage(), e);
            throw e;
        }
        
        consumeResponse(fetchContent, instance);
    }
    
    public abstract void consumeResponse(String content, SomeInstance instance);

	@Override
	public void setIncomingResultSet(ResultsForUpdate<SomeInstance> input) {
		setResultSet(input);
	}

	public FetchSection getFetchSection() {
    	return fetchSection;
    }
	
	@Override
	public String toString() {
		return getModel() + ":CPANDirectFetcher(" + fetchSection + ";" + getResultSet() + ")";
	}

}
