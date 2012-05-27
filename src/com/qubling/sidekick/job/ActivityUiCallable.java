package com.qubling.sidekick.job;

import java.util.concurrent.Callable;

import android.app.Activity;

public interface ActivityUiCallable<V> extends Callable<V> {
	public Activity getActivity();
}
