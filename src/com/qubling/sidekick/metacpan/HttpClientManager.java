/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.metacpan;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * The HTTP client manager lets us reuse an {@link HttpClient} without risking
 * a memory leak (or that's the hope).
 * 
 * @author sterling
 *
 */
public class HttpClientManager {
    private static final String METACPAN_API_USER_AGENT = "CPAN-Sidekick/0.1 (Android)";

    private HttpClient client;
    private int actionsRemaining;
    
    public HttpClientManager() {
    	this.actionsRemaining = 0;
    }

    public HttpClientManager(int actionsRemaining) {
        this.actionsRemaining = actionsRemaining;
    }
    
    private void setupClient() {
        try {
            this.client = (HttpClient) Class.forName("android.net.http.AndroidHttpClient")
                .getMethod("newInstance", String.class).invoke(null, METACPAN_API_USER_AGENT);
        }
        catch (Throwable t) {
            this.client = new DefaultHttpClient();
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
    
    private void cleanupClient() {
    	if (isClientAvailable() && isComplete()) {
            try {
                Class.forName("android.net.http.AndroidHttpClient").getMethod("close").invoke(client);
            }
            catch (Throwable t) {
                // ignore
            }
            client = null;
    	}
    }
    
    public synchronized void attachAction(int count) {
    	actionsRemaining += count;
    	
    	if (!isClientAvailable()) {
    		setupClient();
    	}
    }
    
    public synchronized void attachAction() {
    	actionsRemaining++;
    	
    	if (!isClientAvailable()) {
    		setupClient();
    	}
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
