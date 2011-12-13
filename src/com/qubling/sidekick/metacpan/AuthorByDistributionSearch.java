package com.qubling.sidekick.metacpan;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;

public class AuthorByDistributionSearch extends MetaCPANSearch<Void> {
	
	private ModuleList moduleList;
	private Map<String, Module> authorMap;
	private int countGravatarURLs = 0;
	
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

	@Override
	public Void constructCompiledResult(JSONObject results)
			throws JSONException {
		
		JSONArray hits = results.getJSONObject("hits").getJSONArray("hits");
		
		for (int i = 0; i < hits.length(); i++) {
			JSONObject author = hits.getJSONObject(i).getJSONObject("_source");
			
			String pauseId = author.getString("pauseid");
			String gravatarURL = author.getString("gravatar_url");
			
			Module module = authorMap.get(pauseId);
			if (module != null) {
				module.setAuthorGravatarURL(gravatarURL);
				countGravatarURLs++;
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		HttpClientManager clientManager = new HttpClientManager(countGravatarURLs);
		for (Module module : moduleList) {
			if (module.getAuthorGravatarURL() == null)
				continue;
			
			new GravatarFetcher(getContext(), clientManager, moduleList).execute(module);
		}
	}
}
