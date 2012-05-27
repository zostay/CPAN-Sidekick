package com.qubling.sidekick.job;

import java.util.concurrent.Callable;


public interface ControlCallable<V> extends Callable<V> {
	public void setJobManager(JobManager jobManager);
}
