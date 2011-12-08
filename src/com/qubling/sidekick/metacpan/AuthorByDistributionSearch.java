package com.qubling.sidekick.metacpan;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;

public class AuthorByDistributionSearch extends MetaCPANSearch<Void> {
	
	private static final float GRAVATAR_DP_SIZE = 35f;
	private static final Pattern RESIZE_GRAVATAR_PATTERN = Pattern.compile("([?&])s=[0-9]+\\b");
	
	private ModuleList moduleList;
	private Map<String, Module> authorMap;
	
	public AuthorByDistributionSearch(HttpClientManager clientManager, Context context, ModuleList moduleList) {
		super(clientManager, context, SearchSection.AUTHOR, "author_by_pauseid");
		
		this.moduleList = moduleList;
		this.authorMap  = new HashMap<String, Module>();
		
		this.setSize(0);
	}

	@Override
	public JSONObject search() {
		JSONFragment authors = new JSONFragment() {
			
			@Override
			public String toJSONString() {
				JSONArray terms = new JSONArray();
				
				try {
					for (Module module : moduleList) {
						if (AuthorByDistributionSearch.this.authorMap.containsKey(module.getAuthorPauseId())) 
							continue;
						
						Log.d("AuthorByDistributionSearch", "Adding Author: " + module.getAuthorPauseId());
						
						AuthorByDistributionSearch.this.authorMap.put(module.getAuthorPauseId(), module);
						
						JSONObject pauseid = new JSONObject()
								.put("pauseid", module.getAuthorPauseId());
						
						JSONObject term = new JSONObject()
								.put("term", pauseid);
						
						terms.put(term);
					}
				}
				catch (JSONException e) {
					throw new RuntimeException("error while building JSON", e);
				}
				
				return terms.toString();
			}
		};
		
		Map<String, Object> variables = buildVariables(
			"pauseids", authors	
		);
		
		return makeMetaCPANRequest(variables);
	}
	
	private Bitmap fetchBitmap(String gravatarURL) {
		
		// Calculate the pixel size of the Gravatar
		int gravatarPixelSize = Math.min(
				(int) (GRAVATAR_DP_SIZE * getContext().getResources().getDisplayMetrics().density + 0.5f),
				512);
		
		Matcher resizeGravatarMatcher = RESIZE_GRAVATAR_PATTERN.matcher(gravatarURL);
		String resizedGravatarURL = resizeGravatarMatcher.replaceFirst("$1s=" + gravatarPixelSize);
		
		try {
			
			// Do the request
			Log.d("AuthorByDistributionSearch", "Gravatar: " + resizedGravatarURL);
			HttpGet req = new HttpGet(resizedGravatarURL);
			HttpResponse res = getClient().execute(req);
			
			// Get the response content
			HttpEntity entity = res.getEntity();
			InputStream content = entity.getContent();
			Bitmap gravatarBitmap = BitmapFactory.decodeStream(content);
			return gravatarBitmap;
		}
		
		catch (IOException e) {
			// TODO Return a generic image when this happens
			Log.e("AuthorByDistributionSearch", "Error loading Gravatar: " + e);
			return null;
		}
	}

	@Override
	public Void constructCompiledResult(JSONObject results)
			throws JSONException {
		
		JSONArray hits = results.getJSONObject("hits").getJSONArray("hits");
		
		for (int i = 0; i < hits.length(); i++) {
			JSONObject author = hits.getJSONObject(i).getJSONObject("_source");
			
			String pauseId = author.getString("pauseid");
			String gravatarURL = author.getString("gravatar_url");
			
			Bitmap gravatarBitmap = fetchBitmap(gravatarURL);
			
			Module module = authorMap.get(pauseId);
			if (module != null) {
				module.setAuthorGravatarBitmap(gravatarBitmap);
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		moduleList.notifyModuleListUpdaters();
	}
}
