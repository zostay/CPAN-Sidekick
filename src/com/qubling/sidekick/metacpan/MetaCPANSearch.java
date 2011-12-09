package com.qubling.sidekick.metacpan;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public abstract class MetaCPANSearch<ResultType> extends AsyncTask<Void, Void, ResultType> {
	
	public enum SearchSection {
		AUTHOR ("/v0/author/_search"),
		FAVORITE ("/v0/favorite/_search"),
		FILE ("/v0/file/_search"),
		RATING ("/v0/rating/_search"),
		RELEASE ("/v0/release/_search");
		
		private String path;
		
		SearchSection(String path) {
			this.path = path;
		}
		
		public String getPath() {
			return path;
		}
	}

	private static final String METACPAN_API_URL = "http://api.metacpan.org";
	
	public static final int DEFAULT_SIZE = 10;
	public static final int DEFAULT_FROM = 0;
	
	private HttpClientManager clientManager;
	private Context context;
	private SearchSection searchSection;
	private String searchTemplate;
	private int size = DEFAULT_SIZE;
	private int from = DEFAULT_FROM;
	
	public MetaCPANSearch(HttpClientManager clientManager, Context context, SearchSection searchSection, String searchTemplate) {
		this.clientManager  = clientManager;
		this.context        = context;
		this.searchSection  = searchSection;
		this.searchTemplate = searchTemplate + ".json";
	}
	
	protected Map<String, Object> buildVariables(Object... pairs) {
		Map<String, Object> variables = new HashMap<String, Object>();
		
		variables.put("from", from);
		variables.put("size", size);
		
		for (int i = 0; i < pairs.length; i += 2) {
			String name  = (String) pairs[i];
			Object value = pairs[i + 1];
			
			variables.put(name, value);
		}
		
		return variables;
	}

	protected JSONObject makeMetaCPANRequest(Map<String, Object> variables) {
		StringTemplate templater = new StringTemplate(context);
		String json = templater.processTemplate(searchTemplate, variables);
		
		Log.d("MetaCPANSearch", "REQ " + searchSection.getPath() + ": " + json);
		
		try {
			
			// Setup the REST API request
			HttpPost req = new HttpPost(METACPAN_API_URL + searchSection.getPath());
			req.setEntity(new StringEntity(json));
			
			// Make the request
			HttpResponse res = getClient().execute(req);
			
			// Get the response content
			HttpEntity entity = res.getEntity();
			InputStream content = entity.getContent();
//			Log.d("ModuleSearch", "res.getHeaders(\"Content-Type\"): " + res.getHeaders("Content-Type")[0].getValue());
//			Log.d("ModuleSearch", "entity.getContentType(): " + entity.getContentType());
			
			// Determine the charset
			String charset;
			String contentType = entity.getContentType().getValue();
			int charsetIndex = contentType.indexOf("charset=");
			if (charsetIndex >= 0) {
				int endingIndex = contentType.indexOf(";", charsetIndex + 8);
				if (endingIndex >= 0) {
					charset = contentType.substring(charsetIndex + 8, endingIndex);
				}
				else {
					charset = contentType.substring(charsetIndex + 8);
				}
			}
			else {
				charset = "UTF-8";
			}

			// Read the content
//			Log.d("ModuleSearch", "charset: " + charset);
			InputStreamReader contentReader = new InputStreamReader(content, charset);
			char[] buf = new char[1000];
			StringBuilder contentStr = new StringBuilder();
			int readLength;
			while ((readLength = contentReader.read(buf)) > -1) {
				contentStr.append(buf, 0, readLength);
			}
			
			Log.d("MetaCPANSearch", "RES " + searchSection.getPath() + ": " + contentStr.toString());
			
			// Parse the response into JSON and return it
			Object parsedContent = new JSONTokener(contentStr.toString()).nextValue();
			if (parsedContent instanceof JSONObject) {
				return (JSONObject) parsedContent;
			}
			else {
				// TODO Show an alert dialog when this happens
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
	
	public HttpClientManager getClientManager() {
		return clientManager;
	}

	public AndroidHttpClient getClient() {
		return clientManager.getClient();
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
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
	
	public abstract JSONObject search();
	public abstract ResultType constructCompiledResult(JSONObject results) throws JSONException;

	@Override
	protected ResultType doInBackground(Void... params) {
		
		try {
			JSONObject results = search();
			if (results == null) return null;
			
			return constructCompiledResult(results);
		}
		catch (JSONException e) {
			// TODO Show an alert dialog if this should ever happen
			Log.e("ModuleSearch", e.toString());
			return null;
		}
		finally {
			clientManager.markActionCompleted();
		}		
	}

}