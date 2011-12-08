package com.qubling.sidekick.metacpan;

import android.net.http.AndroidHttpClient;

public class HttpClientManager {
	private static final String METACPAN_API_USER_AGENT = "CPAN-Sidekick/0.1 (Android)";
	
	private AndroidHttpClient client;
	private int actionsRemaining;
	
	public HttpClientManager(int actionsRemaining) {
		super();
		
		this.actionsRemaining = actionsRemaining;
		this.client = AndroidHttpClient.newInstance(METACPAN_API_USER_AGENT);
	}

	public synchronized AndroidHttpClient getClient() {
		return client;
	}
	
	public synchronized boolean isComplete() {
		return actionsRemaining <= 0;
	}
	
	public synchronized void markActionCompleted() {
		actionsRemaining--;
		
		if (isComplete()) {
			client.close();
			client = null;
		}
	}
}
