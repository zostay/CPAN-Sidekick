/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.metacpan;

import java.util.Collection;
import java.util.HashSet;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

/**
 * The HTTP client manager lets us reuse an {@link HttpClient} without risking
 * a memory leak (or that's the hope).
 * 
 * @author sterling
 *
 */
public class HttpClientManager {
	
	// TODO Set this up from a resource string somehow
    private static final String METACPAN_API_USER_AGENT = "CPAN-Sidekick/0.1 (Android)";
    
    public static interface OnHttpClientAction {
    	public void onActionsStart();
    	public void onActionsComplete();
    }
    
    private Collection<OnHttpClientAction> listeners = new HashSet<OnHttpClientAction>();

    private HttpClient client;
    private int actionsRemaining;
    
    public HttpClientManager() {
    	this.actionsRemaining = 0;
    }

    public HttpClientManager(int actionsRemaining) {
        this.actionsRemaining = actionsRemaining;
        
        if (actionsRemaining > 0) setupClient();
    }
    
    public synchronized void addOnHttpClientActionListener(OnHttpClientAction listener) {
    	listeners.add(listener);
    }
    
    public synchronized void removeOnHttpClientActionListener(OnHttpClientAction listener) {
    	listeners.remove(listener);
    }
    
    public synchronized void notifyActionsStart() {
    	for (OnHttpClientAction listener : listeners) {
    		listener.onActionsStart();
    	}
    }
    
    public synchronized void notifyActionsComplete() {
    	for (OnHttpClientAction listener : listeners) {
    		listener.onActionsComplete();
    	}
    }
    
    private void setupClient() {
    	if (!isClientAvailable()) {
	        try {
	            client = (HttpClient) Class.forName("android.net.http.AndroidHttpClient")
	                .getMethod("newInstance", String.class).invoke(null, METACPAN_API_USER_AGENT);
	            Log.i("HttpClientManager", "Using AndroidHttpClient");
	        }
	        catch (Throwable t) {
	        	Log.i("HttpClientManager", "Falling back to DefaultHttpClient");
	            client = new DefaultHttpClient();
	        }
	        
	        notifyActionsStart();
    	}
    }
    
    private void cleanupClient() {
    	Log.d("HttpClientManager", "Actions Remaining: " + actionsRemaining);
    	if (isClientAvailable() && isComplete()) {
            try {
                Class.forName("android.net.http.AndroidHttpClient").getMethod("close").invoke(client);
            }
            catch (Throwable t) {
                // ignore
            }
            client = null;
        	
        	notifyActionsComplete();
    	}
    }

    public synchronized HttpClient getClient() {
        if (isComplete())
            throw new IllegalStateException("Cannot fetch client again after last action has completed.");

        return client;
    }

    public synchronized boolean isComplete() {
        return actionsRemaining <= 0;
    }
    
    private boolean isClientAvailable() {
    	return client != null;
    }
    
    public synchronized void attachAction(int count) {
    	Log.d("HttpClientManager", "attachAction(" + count + ")");
    	actionsRemaining += count;
    	setupClient();
    }
    
    public synchronized void attachAction() {
    	Log.d("HttpClientManager", "attachAction()");
    	actionsRemaining++;
    	setupClient();
    }

    public synchronized void markActionCompleted(int count) {
        actionsRemaining -= count;
        cleanupClient();
    }

    public synchronized void markActionCompleted() {
    	actionsRemaining--;
    	cleanupClient();
    }
}
