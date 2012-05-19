package com.qubling.sidekick.model;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.util.Log;

public abstract class CPANDirectFetcher<SomeInstance extends Instance<SomeInstance>> 
	extends CPANFetcher<SomeInstance> implements UpdateFetcher<SomeInstance> {
	
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
    
    public CPANDirectFetcher(Model<SomeInstance> model, FetchSection fetchSection) {
    	super(model);
    	
    	this.fetchSection  = fetchSection;
    }
	
	@Override
	public ExecutorService getPreferredExecutor() {
		return getSchema().getControlExecutor();
	}

	@Override
	protected ResultSet<SomeInstance> execute() throws IOException, InterruptedException {
		final CountDownLatch latch = new CountDownLatch(getResultSet().size());
		ExecutorService service = getSchema().getJobExecutor();
		
		for (final SomeInstance instance : getResultSet()) {
			service.submit(new Callable<SomeInstance>() {
				@Override
				public SomeInstance call() throws IOException {
					CPANDirectFetcher.this.fetchOne(instance);
					latch.countDown();
					return instance;
				}
			});
		}
		
		latch.await();
		
		return getResultSet();
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
