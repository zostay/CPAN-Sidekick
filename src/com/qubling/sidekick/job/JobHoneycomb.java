package com.qubling.sidekick.job;

import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;

public class JobHoneycomb extends Job {
    
    public JobHoneycomb(Activity activity) {
        super(activity);
    }

    @Override
    public void executeJob(JobExecutor job) throws RejectedExecutionException {
        job.executeOnExecutor(JobExecutor.THREAD_POOL_EXECUTOR);
    }

}
