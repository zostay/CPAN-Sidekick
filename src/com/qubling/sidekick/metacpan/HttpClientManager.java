/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.metacpan;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;


public class HttpClientManager {
    private static final String METACPAN_API_USER_AGENT = "CPAN-Sidekick/0.1 (Android)";

    private HttpClient client;
    private int actionsRemaining;

    public HttpClientManager(int actionsRemaining) {
        super();

        this.actionsRemaining = actionsRemaining;

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

    public synchronized void markActionCompleted(int count) {
        actionsRemaining -= count;

        if (isComplete()) {
            try {
                Class.forName("android.net.http.AndroidHttpClient").getMethod("close").invoke(client);
            }
            catch (Throwable t) {
                // ignore
            }
            client = null;
        }

    }

    public synchronized void markActionCompleted() {
        markActionCompleted(1);
    }
}
