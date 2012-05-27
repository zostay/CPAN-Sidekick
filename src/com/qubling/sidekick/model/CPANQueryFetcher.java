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

public abstract class CPANQueryFetcher<SomeInstance extends Instance<SomeInstance>> 
	extends CPANFetcher<SomeInstance> implements LimitedFetcher<SomeInstance> {

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

    public static final int DEFAULT_SIZE = 10;
    public static final int DEFAULT_FROM = 0;
    
    private final SearchSection searchSection;
    private final String searchTemplate;
    
    private int size = DEFAULT_SIZE;
    private int from = DEFAULT_FROM;
    
    public CPANQueryFetcher(Model<SomeInstance> model, SearchSection searchSection, String searchTemplate) {
    	super(model);
    	
    	this.searchSection   = searchSection;
    	this.searchTemplate  = searchTemplate + ".json";
    }
    
    protected void execute() {
    	Log.d("CPANQueryFetcher", "START execute()");
    	
    	HashMap<String, Object> variables = new HashMap<String, Object>();
    	variables.put("from", from);
    	variables.put("size", size);
    	
    	prepareRequest(variables);
    	Log.d("CPANQueryFetcher", "Setup variables by calling prepareRequest()");
    	JSONObject searchResponse = makeMetaCPANRequest(variables);
    	
    	try {
    		consumeResponse(searchResponse);
    	}
    	
    	// TODO Notify BugSense if this happens...
    	catch (JSONException e) {
    		Log.e("CPANQueryFetcher", "Error while reading JSON during query: " + e.getMessage(), e);
    	}
    	
    	Log.d("CPANQueryFetcher", "END execute()");
    }
    
    protected abstract void prepareRequest(Map<String, Object> variables);
    protected abstract void consumeResponse(JSONObject searchResponse) throws JSONException;

    private JSONObject makeMetaCPANRequest(Map<String, Object> variables) {
    	Log.d("CPANQueryFetcher", "START makeMetaCPANRequest()");
    	
        StringTemplate templater = new StringTemplate(getContext());
        String json = templater.processTemplate(searchTemplate, variables);

        Log.d("CPANQueryFetcher", "REQ " + searchSection.getPath() + ": " + json);

        try {

            // Setup the REST API request
            HttpPost req = new HttpPost(METACPAN_API_URL + searchSection.getPath());
            req.setEntity(new StringEntity(json));

            // Make the request
            HttpResponse res = getHttpClient().execute(req);

            // Read the content
            String content = slurpContent(res);

            Log.d("CPANQueryFetcher", "RES " + searchSection.getPath() + ": " + content);

            // Parse the response into JSON and return it
            Object parsedContent = new JSONTokener(content).nextValue();
            if (parsedContent instanceof JSONObject) {
                return (JSONObject) parsedContent;
            }
            else {
                // TODO Show an alert dialog or toast when this happens
                Log.e("CPANQueryFetcher", "Unexpected JSON content: " + parsedContent);
                return null;
            }
        }
        catch (UnsupportedCharsetException e) {
            // TODO Show an alert dialog if this should ever happen
            Log.e("CPANQueryFetcher", e.toString());
        }
        catch (IOException e) {
            // TODO Show an alert dialog if this should ever happen
            Log.e("CPANQueryFetcher", e.toString());
        }
        catch (JSONException e) {
            // TODO Show an alert dialog if this should ever happen
            Log.e("CPANQueryFetcher", e.toString());
        }
        
        Log.d("CPANQueryFetcher", "END makeMetaCPANRequest()");

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
	
	@Override
	public String toString() {
		return getModel() + ":CPANQueryFetcher(" + searchSection + ";" + searchTemplate.hashCode() + ")";
	}
}
