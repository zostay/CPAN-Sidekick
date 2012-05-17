package com.qubling.sidekick.model;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.qubling.sidekick.api.HttpClientManager;

import android.content.Context;

public class Schema {
	private final static int JOB_THREAD_POOL_SIZE = 6;
	
	private final GravatarModel gravatarModel;
	private final AuthorModel authorModel;
	private final ReleaseModel releaseModel;
	private final ModuleModel moduleModel;
	
	private ExecutorService jobExecutor, controlExecutor;
	private Context context;
	private HttpClientManager clientManager;
	
	public Schema(Context context, HttpClientManager clientManager) {
		gravatarModel = new GravatarModel(this);
		authorModel = new AuthorModel(this);
		releaseModel = new ReleaseModel(this);
		moduleModel = new ModuleModel(this);
		
		jobExecutor = Executors.newFixedThreadPool(JOB_THREAD_POOL_SIZE);
		controlExecutor = Executors.newCachedThreadPool();
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

	public HttpClientManager getClientManager() {
    	return clientManager;
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
	
	public <SomeInstance extends Instance<SomeInstance>> Search<SomeInstance> doFetch(Fetcher<SomeInstance> fetcher) {
		return new Search<SomeInstance>(controlExecutor, jobExecutor, fetcher);
	}
}
