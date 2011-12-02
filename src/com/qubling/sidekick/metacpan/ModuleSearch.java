package com.qubling.sidekick.metacpan;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Formatter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.qubling.sidekick.R;
import com.qubling.sidekick.metacpan.model.Module;

import android.content.res.Resources;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ModuleSearch extends AsyncTask<Void, Void, JSONObject> {
	
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

	@Override
	protected JSONObject doInBackground(Void... params) {
		AndroidHttpClient client = AndroidHttpClient.newInstance("CPAN-Sidekick/0.1 (Android)");
		
		// Load the JSON query template
		Resources resources = updateView.getResources();
		String moduleSearchTemplate;
		try {
			InputStream moduleSearchTemplateIn = resources.getAssets().open("module_search_template.json");
			InputStreamReader moduleSearchTemplateReader = new InputStreamReader(moduleSearchTemplateIn, "UTF-8");
			char[] buf = new char[1000];
			StringBuilder moduleSearchTemplateBuilder = new StringBuilder();
			int readLength;
			while ((readLength = moduleSearchTemplateReader.read(buf)) > -1) {
				moduleSearchTemplateBuilder.append(buf, 0, readLength);
			}
			moduleSearchTemplate = moduleSearchTemplateBuilder.toString();
		}
		catch (IOException e) {
			// TODO Should we do something about this?
			Log.e("ModuleSearch", "Error loading module_search_template.json: " + e.getMessage());
			return null;
		}
		
		// Format the query into the actual JSON to run
		String cleanQuery = query.replace("::", " ");
		Formatter moduleSearchJSON = new Formatter();
		moduleSearchJSON.format(moduleSearchTemplate, query, cleanQuery, size, from);
		
		Log.d("ModuleSearch", moduleSearchJSON.toString());
		
		try {
			
			// Setup the REST API request
			HttpPost req = new HttpPost(METACPAN_API_URL + "/v0/file/_search");
			req.setEntity(new StringEntity(moduleSearchJSON.toString()));
			
			// Make the request
			HttpResponse res = client.execute(req);
			
			// Get the response content
			HttpEntity entity = res.getEntity();
			InputStream content = entity.getContent();
			Log.d("ModuleSearch", "res.getHeaders(\"Content-Type\"): " + res.getHeaders("Content-Type")[0].getValue());
			Log.d("ModuleSearch", "entity.getContentType(): " + entity.getContentType());
			
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
			Log.d("ModuleSearch", "charset: " + charset);
			InputStreamReader contentReader = new InputStreamReader(content, charset);
			char[] buf = new char[1000];
			StringBuilder contentStr = new StringBuilder();
			int readLength;
			while ((readLength = contentReader.read(buf)) > -1) {
				contentStr.append(buf, 0, readLength);
			}
			
			Log.d("ModuleSearch", contentStr.toString());
			
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
		finally {
			client.close();
		}

		return null;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		
		// Nothing useful returned, forget it
		if (result == null) return;
		
		try {
			
			// Slurp up the matches
			JSONArray hits = result.getJSONObject("hits").getJSONArray("hits");
			int modulesCount = hits.length();
			Module[] modules = new Module[modulesCount];
			for (int i = 0; i < hits.length(); i++) {
				JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");
				
				if (hit.has("module")) {
					modules[i] = new Module(
						hit.getJSONArray("module").getJSONObject(0).getString("name")
					);
				}
				else {
					modules[i] = new Module(hit.getString("name"));
				}
			}
			
			// Stuff the matches into an adapter and fill the list view
			ModuleSearchAdapter resultAdapter = new ModuleSearchAdapter(updateView.getContext(), R.layout.module_search_list_item, modules);
			updateView.setAdapter(resultAdapter);
		}
		catch (JSONException e) {
			// TODO Show an alert dialog if this should ever happen
			Log.e("ModuleSearch", e.toString());
		}
	}

}
