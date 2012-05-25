package com.qubling.sidekick.model;

import java.util.concurrent.Callable;

import android.app.Activity;

public interface ActivityUiCallable<V> extends Callable<V> {
	public Activity getActivity();
}
