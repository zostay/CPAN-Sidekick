package com.qubling.sidekick.job;

import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;

public class JobEclair extends Job {
    
    public JobEclair(Activity activity) {
        super(activity);
    }

    @Override
    public void executeJob(JobExecutor job) throws RejectedExecutionException {
        job.execute();
    }

}
