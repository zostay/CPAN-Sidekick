package com.qubling.sidekick.model;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import com.qubling.sidekick.api.StringTemplate;

public class CPANQueryFetcher<SomeInstance extends Instance> extends CPANFetcher<SomeInstance> {

	/**
	 * An enumeration of search types on MetaCPAN.
	 *
	 * @author sterling
	 *
	 */
    public enum SearchSection {
        AUTHOR ("v0/author/_search"),
        FAVORITE ("v0/favorite/_search"),
        FILE ("v0/file/_search"),
        RATING ("v0/rating/_search"),
        RELEASE ("v0/release/_search");

        private String path;

        SearchSection(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }
    
    public interface SearchCallback<SomeInstance extends Instance> {
    	public void prepareRequest(Map<String, Object> variables);
    	public void consumeResponse(JSONObject response, ResultSet<SomeInstance> results) throws JSONException;
    }

    public static final int DEFAULT_SIZE = 10;
    public static final int DEFAULT_FROM = 0;
    
    private final SearchSection searchSection;
    private final String searchTemplate;
    private final SearchCallback<SomeInstance> searchCallback;
    
    private int size = DEFAULT_SIZE;
    private int from = DEFAULT_FROM;
    
    public CPANQueryFetcher(SearchSection searchSection, String searchTemplate, SearchCallback<SomeInstance> searchCallback) {
    	this.searchSection   = searchSection;
    	this.searchTemplate  = searchTemplate;
    	this.searchCallback = searchCallback;
    }
    
    protected ResultSet<SomeInstance> execute() {
    	HashMap<String, Object> variables = new HashMap<String, Object>();
    	variables.put("from", from);
    	variables.put("size", size);
    	
    	getSearchCallback().prepareRequest(variables);
    	JSONObject searchResponse = makeMetaCPANRequest(variables);
    	
    	try {
    		searchCallback.consumeResponse(searchResponse, getResultSet());
    	}
    	
    	// TODO Notify BugSense if this happens...
    	catch (JSONException e) {
    		Log.e("CPANQueryFetcher", "Error while reading JSON during query: " + e.getMessage(), e);
    	}
    	
    	return getResultSet();
    }

    private JSONObject makeMetaCPANRequest(Map<String, Object> variables) {
        StringTemplate templater = new StringTemplate(getContext());
        String json = templater.processTemplate(searchTemplate, variables);

//        Log.d("MetaCPANSearch", "REQ " + searchSection.getPath() + ": " + json);

        try {

            // Setup the REST API request
            HttpPost req = new HttpPost(METACPAN_API_URL + searchSection.getPath());
            req.setEntity(new StringEntity(json));

            // Make the request
            HttpResponse res = getHttpClient().execute(req);

            // Read the content
            String content = slurpContent(res);

//            Log.d("MetaCPANSearch", "RES " + searchSection.getPath() + ": " + content);

            // Parse the response into JSON and return it
            Object parsedContent = new JSONTokener(content).nextValue();
            if (parsedContent instanceof JSONObject) {
                return (JSONObject) parsedContent;
            }
            else {
                // TODO Show an alert dialog or toast when this happens
                Log.e("MetaCPANSearch", "Unexpected JSON content: " + parsedContent);
                return null;
            }
        }
        catch (UnsupportedCharsetException e) {
            // TODO Show an alert dialog if this should ever happen
            Log.e("MetaCPANSearch", e.toString());
        }
        catch (IOException e) {
            // TODO Show an alert dialog if this should ever happen
            Log.e("MetaCPANSearch", e.toString());
        }
        catch (JSONException e) {
            // TODO Show an alert dialog if this should ever happen
            Log.e("MetaCPANSearch", e.toString());
        }

        return null;
    }

	public int getSize() {
    	return size;
    }

	public void setSize(int size) {
    	this.size = size;
    }

	public int getFrom() {
    	return from;
    }

	public void setFrom(int from) {
    	this.from = from;
    }

	public SearchSection getSearchSection() {
    	return searchSection;
    }

	public String getSearchTemplate() {
    	return searchTemplate;
    }

	public SearchCallback<SomeInstance> getSearchCallback() {
    	return searchCallback;
    }
}
