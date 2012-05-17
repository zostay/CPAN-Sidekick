package com.qubling.sidekick.model;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.util.Log;

public class CPANDirectFetcher<SomeInstance extends Instance<SomeInstance>> 
	extends CPANFetcher<SomeInstance> {
	
	/**
	 * An enumeration of direct retrieval URLs on MetaCPAN.
	 *
	 * @author sterling
	 *
	 */
    public enum FetchSection {
    	MODULE_POD(METACPAN_API_POD_URL),
    	MODULE_FETCH(METACPAN_API_MODULE_URL);

        private String baseUrl;

        FetchSection(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getBaseUrl() {
            return baseUrl;
        }
    }
    
    public interface FetchCallback<SomeInstance extends Instance<SomeInstance>> {
    	public void consumeResponse(String content, ResultSet<SomeInstance> results);
    }
    
    private FetchSection fetchSection;
    private String urlSuffix;
    private FetchCallback<SomeInstance> fetchCallback;
    
    public CPANDirectFetcher(Model<SomeInstance> model, FetchSection fetchSection, String urlSuffix, FetchCallback<SomeInstance> fetchCallback) {
    	super(model);
    	
    	this.fetchSection  = fetchSection;
    	this.urlSuffix     = urlSuffix;
    	this.fetchCallback = fetchCallback;
    }

	@Override
    protected ResultSet<SomeInstance> execute() throws IOException {
        String fetchContent;
        try {
            HttpGet fetchRequest = new HttpGet(fetchSection.getBaseUrl() + urlSuffix);
            HttpResponse fetchResponse = getHttpClient().execute(fetchRequest);

            fetchContent = slurpContent(fetchResponse);
        }

        catch (IOException e) {
            Log.e("CPANDirectFetcher", "Cannot fetch from " + fetchSection.getBaseUrl() + urlSuffix + ": " + e.getMessage(), e);
            throw e;
        }
        
        fetchCallback.consumeResponse(fetchContent, getResultSet());

	    return getResultSet();
    }

	public FetchSection getFetchSection() {
    	return fetchSection;
    }

	public String getUrlSuffix() {
    	return urlSuffix;
    }

	public FetchCallback<SomeInstance> getFetchCallback() {
    	return fetchCallback;
    }

}
