package com.qubling.sidekick.model;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import com.qubling.sidekick.R;
import com.qubling.sidekick.model.Search.OnSearchActivity;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class Schema implements OnSearchActivity {
	private final static int JOB_THREAD_POOL_SIZE = 3;
	private static final String METACPAN_API_USER_AGENT_SUFFIX = " (Android)";
	
	private final GravatarModel gravatarModel;
	private final AuthorModel authorModel;
	private final ReleaseModel releaseModel;
	private final ModuleModel moduleModel;
	
	private int runningSearches = 0;
	
	private ExecutorService jobExecutor, controlExecutor;
	private Context context;
	private HttpClient httpClient;
	
	public Schema(Context context) {
		gravatarModel = new GravatarModel(this);
		authorModel = new AuthorModel(this);
		releaseModel = new ReleaseModel(this);
		moduleModel = new ModuleModel(this);
		
		this.context = context;
		
		initialize();
	}
	
	private void initialize() {
		jobExecutor = Executors.newFixedThreadPool(JOB_THREAD_POOL_SIZE);
		controlExecutor = Executors.newCachedThreadPool();
	}
	
    private void setupHttpClient() {
        try {
    		
    		String userAgent = context.getString(R.string.app_name)
    				         + "/"
    				         + context.getString(R.string.app_version)
    				         + METACPAN_API_USER_AGENT_SUFFIX;
    		
            httpClient = (HttpClient) Class.forName("android.net.http.AndroidHttpClient")
                .getMethod("newInstance", String.class).invoke(null, userAgent);
            Log.i("HttpClientManager", "Using AndroidHttpClient");
        }
        catch (Throwable t) {
        	Log.i("HttpClientManager", "Falling back to DefaultHttpClient");
            httpClient = new DefaultHttpClient();
        }
    }
    
    public void closeHttpClient() {
    	if (httpClient instanceof AndroidHttpClient) {
    		((AndroidHttpClient) httpClient).close();
    	}
    	
    	httpClient = null;
    }
	
	public GravatarModel getGravatarModel() {
		return gravatarModel;
	}
	
	public AuthorModel getAuthorModel() {
    	return authorModel;
    }

	public ReleaseModel getReleaseModel() {
    	return releaseModel;
    }

	public ModuleModel getModuleModel() {
    	return moduleModel;
    }
	
	public Context getContext() {
    	return context;
    }

	public HttpClient getHttpClient() {
    	return httpClient;
    }
	
	/**
	 * Retrieves an {@link ExecutorService} that is used for job threads. There 
	 * are only a limited number of job threads available and so a job will wait 
	 * until the previous job completes. Jobs should never be allowed to block 
	 * to avoid a deadlock if all the available threads are used up and no more 
	 * are left.
	 * 
	 * @return an {@link ExecutorService} for running jobs
	 */
	public ExecutorService getJobExecutor() {
		return jobExecutor;
	}
	
	/**
	 * Retrieves an {@link ExecutorService} that is used for control threads. 
	 * These are threads that do a tiny bit of work for setup and then spend 
	 * most of their life blocking and waiting for jobs to complete. Normally, 
	 * the {@link #getJobExecutor()} should be preferred.
	 * 
	 * @return an {@link ExecutorService} for running job controllers
	 */
	public ExecutorService getControlExecutor() {
		return controlExecutor;
	}
	
	public void cancelSearch() {
		jobExecutor.shutdown();
		controlExecutor.shutdown();
		
		// We just assume shutdown. I hope that's okay.
		
		initialize();
	}
	
	public <SomeInstance extends Instance<SomeInstance>> Search<SomeInstance> doFetch(Fetcher<SomeInstance> fetcher) {
		Search<SomeInstance> search = new Search<SomeInstance>(controlExecutor, jobExecutor, fetcher);
		search.addOnSearchActivityListener(this);
		return search;
	}
	
	@Override
	public synchronized void onSearchStart() {
		Log.d("Schema", "onSearchStart()");
		if (runningSearches == 0) setupHttpClient();
		runningSearches++;
	}
	
	@Override
	public synchronized void onSearchComplete() {
		Log.d("Schema", "onSearchComplete()");
		runningSearches--;
		if (runningSearches == 0) closeHttpClient();
	}
}
