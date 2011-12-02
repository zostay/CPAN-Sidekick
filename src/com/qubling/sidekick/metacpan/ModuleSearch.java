package com.qubling.sidekick.metacpan;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Formatter;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.qubling.sidekick.R;
import com.qubling.sidekick.metacpan.result.Module;

import android.content.res.Resources;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

public class ModuleSearch extends AsyncTask<Void, Void, Module[]> {
	
	private static final String METACPAN_API_URL = "http://api.metacpan.org";
		
	private static final int DEFAULT_SIZE = 25;
	private static final int DEFAULT_FROM = 0;

	private ListView updateView;
	private String query;
	private int size;
	private int from;
		
	public ModuleSearch(ListView updateView, String query, int size, int from) {
		this.updateView = updateView;
		this.query      = query.replace("\"", "\\\"");
		this.size       = size;
		this.from       = from;
	}
	
	public ModuleSearch(ListView updateView, String query, int size) {
		this(updateView, query, size, DEFAULT_FROM);
	}
	
	public ModuleSearch(ListView updateView, String query) {
		this(updateView, query, DEFAULT_SIZE, DEFAULT_FROM);
	}
	
	private String loadTemplate(String assetName) {
		
		// Load the JSON query template
		Resources resources = updateView.getResources();
		try {
			InputStream moduleSearchTemplateIn = resources.getAssets().open(assetName);
			InputStreamReader moduleSearchTemplateReader = new InputStreamReader(moduleSearchTemplateIn, "UTF-8");
			char[] buf = new char[1000];
			StringBuilder moduleSearchTemplateBuilder = new StringBuilder();
			int readLength;
			while ((readLength = moduleSearchTemplateReader.read(buf)) > -1) {
				moduleSearchTemplateBuilder.append(buf, 0, readLength);
			}
			return moduleSearchTemplateBuilder.toString();
		}
		
		catch (IOException e) {
			// TODO Should we do something about this?
			Log.e("ModuleSearch", "Error loading module_search_template.json: " + e.getMessage());
			return null;
		}
		
	}
	
	private String loadAndFormatTemplate(String assetName, Object... params) {
		String template = loadTemplate(assetName);
	
		// Format the query into the actual JSON to run
		Formatter templateJSON = new Formatter();
		templateJSON.format(template, params);
		
		return templateJSON.toString();
	}
	
	private JSONObject makeMetaCPANRequest(AndroidHttpClient client, String path, String json) {
		try {
			
			// Setup the REST API request
			HttpPost req = new HttpPost(METACPAN_API_URL + path);
			req.setEntity(new StringEntity(json));
			
			// Make the request
			HttpResponse res = client.execute(req);
			
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
			
			Log.d("ModuleSearch", path + ": " + contentStr.toString());
			
			// Parse the response into JSON and return it
			Object parsedContent = new JSONTokener(contentStr.toString()).nextValue();
			if (parsedContent instanceof JSONObject) {
				return (JSONObject) parsedContent;
			}
			else {
				// TODO Show an alert dialog when this happens
				Log.e("ModuleSearch", "Unexpected JSON content: " + parsedContent);
				return null;
			} 
		}
		catch (UnsupportedCharsetException e) {
			// TODO Show an alert dialog if this should ever happen
			Log.e("ModuleSearch", e.toString());
		}
		catch (IOException e) {
			// TODO Show an alert dialog if this should ever happen
			Log.e("ModuleSearch", e.toString());
		}
		catch (JSONException e) {
			// TODO Show an alert dialog if this should ever happen
			Log.e("ModuleSearch", e.toString());
		}

		return null;	
		
	}
	
	private JSONObject search(AndroidHttpClient client) {
		
		// Setup the query
		String cleanQuery = query.replace("::", " ");
		String moduleSearchJSON = loadAndFormatTemplate(
				"module_search_template.json", query, cleanQuery, size, from);
				
		return makeMetaCPANRequest(client, "/v0/file/_search", moduleSearchJSON);
	}
	
	private void setupReleaseRatings(AndroidHttpClient client, Module[] modules) throws JSONException {
		HashMap<String, Module> distMap = new HashMap<String, Module>();
		StringBuilder distributions = new StringBuilder();
		boolean needAnd = false;
		for (Module module : modules) {
			if (distMap.containsKey(module.getDistributionName())) 
				continue;
			
			if (needAnd) distributions.append(", ");
			needAnd = true;
			
			distMap.put(module.getDistributionName(), module);
			
			distributions.append("{ \"term\": { \"rating.distribution\": \"");
			distributions.append(module.getDistributionName().replaceAll("\"", "\\\""));
			distributions.append("\" } }");
		}
		
		String distRatingsJSON = loadAndFormatTemplate(
				"distribution_ratings_template.json", distributions);
		
		JSONObject ratings = makeMetaCPANRequest(client, "/rating/_search", distRatingsJSON);
		
		JSONArray facets = ratings.getJSONObject("facets").getJSONObject("ratings").getJSONArray("terms");
		
		for (int i = 0; i < facets.length(); i++) {
			JSONObject facet = facets.getJSONObject(i);
			
			String distributionName = facet.getString("term"); 
			int ratingCount = facet.getInt("count");
			double ratingMean = facet.getDouble("mean");
			
			Module module = distMap.get(distributionName);
			if (module != null) {
				module.setDistributionRatingCount(ratingCount);
				module.setDistributionRating(ratingMean);
			}
		}
	}
	
	private Module[] constructModuleList(JSONObject searchResult) throws JSONException {
		
		// Slurp up the matches
		JSONArray hits = searchResult.getJSONObject("hits").getJSONArray("hits");
		int modulesCount = hits.length();
		Module[] modules = new Module[modulesCount];
		for (int i = 0; i < hits.length(); i++) {
			JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");
			
			modules[i] = Module.fromModuleSearch(hit);
		}
		
		return modules;
	}

	@Override
	protected Module[] doInBackground(Void... params) {
		AndroidHttpClient client = AndroidHttpClient.newInstance("CPAN-Sidekick/0.1 (Android)");
//		Log.d("ModuleSearch", moduleSearchJSON.toString());
		
		try {
			JSONObject moduleSearch = search(client);
			if (moduleSearch == null) return null;
			
			Module[] modules = constructModuleList(moduleSearch);
			setupReleaseRatings(client, modules);
			return modules;
		}
		catch (JSONException e) {
			// TODO Show an alert dialog if this should ever happen
			Log.e("ModuleSearch", e.toString());
			return null;
		}
		finally {
			client.close();
		}		
	}

	@Override
	protected void onPostExecute(Module[] modules) {
		
		// Nothing useful returned, forget it
		if (modules == null) return;
			
		// Stuff the matches into an adapter and fill the list view
		ModuleSearchAdapter resultAdapter = new ModuleSearchAdapter(updateView.getContext(), R.layout.module_search_list_item, modules);
		updateView.setAdapter(resultAdapter);
	}

}
