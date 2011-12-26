/*
 * Copyright 2011 Qubling Software, LLC.
 * 
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.metacpan;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;
import android.webkit.WebView;

import com.qubling.sidekick.metacpan.result.Module;

public class ModulePODFetcher extends MetaCPANAPI<Module, Void, String> {
	
	private WebView webView;

	public ModulePODFetcher(HttpClientManager clientManager, WebView webView) {
		super(clientManager);
		
		this.webView = webView;
	}

	@Override
	protected String doInBackground(Module... params) {
		Module module = params[0];
		
		String podContent;
		try {
			HttpGet podReq = new HttpGet(METACPAN_API_POD_URL + module.getName());
			HttpResponse podRes = getClient().execute(podReq);
			
			podContent = slurpContent(podRes);
		}
		
		catch (IOException e) {
			Log.e("ModulePODFetcher", "Cannot fetch POD for " + module.getName() + ": " + e);
			return null;
		}
		
		return podContent;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		String wrappedResult = "<html><head><link href=\"style/pod.css\" type=\"text/css\" rel=\"stylesheet\"/></head><body class=\"pod\">"
				             + result
				             + "</body></html>";
	
		webView.loadDataWithBaseURL("file:///android_asset/web/pod/", wrappedResult, "text/html", "UTF-8", null);
	}
}
