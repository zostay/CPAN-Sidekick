package com.qubling.sidekick.fetch.cpan;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.search.ResultSet;
import com.qubling.sidekick.search.Results;

import android.util.Log;

public class ModuleKeywordSearch extends CPANQueryFetcher<Module> {
	private String safeKeywords;
	private String cleanKeywords;
	
	public ModuleKeywordSearch(Model<Module> model, String keywords) {
		super(model, SearchSection.FILE, "module_search");
		
		safeKeywords = keywords.replace("\"", "\\\"");
		cleanKeywords = safeKeywords.replace("::", " ");
	}
	
	@Override
	protected boolean shouldCompleteRequest() {
//		Log.d("ModuleKeywordSearch", "safeKeywords is '" + safeKeywords + "' contains anti-whitespace " + safeKeywords.matches(".*\\S.*"));
		return safeKeywords.matches(".*\\S.*");
	}

	@Override
	protected void prepareRequest(Map<String, Object> variables) {
		variables.put("query", safeKeywords);
		variables.put("cleanQuery", cleanKeywords);
	}
	
	@Override
    public void consumeResponse(JSONObject response) throws JSONException {
		ResultSet<Module> results = getResultSet();
		
		if (response == null) {
		    Log.e("ModuleKeywordSearch", "Unexpected response (response is null)");
		    return;
		}
		
		JSONObject topHits = response.getJSONObject("hits");
		if (topHits == null) {
			Log.e("ModuleKeywordSearch", "Unexpected response (top hits missing): " + response);
			return;
		}
		
        JSONArray hits = topHits.getJSONArray("hits");
        if (hits == null) {
        	Log.e("ModuleKeywordSearch", "Unexpected response (nested hits missing): " + response);
        	return;
        }

        // Slurp up the matches
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");
            
	        String name = null;
	        try {
	            if (hit.has("module")) {
	                name = hit.getJSONArray("module").getJSONObject(0).getString("name");
	            }
	            else {
	                name = hit.getString("name");
	            }
	        }
	        catch (JSONException e) {
	            name = "Unknown Module Name";
	        }

	        String moduleAbstract = null;
	        String authorPauseId  = null;
	        String releaseName    = null;
	        String releaseVersion = null;

	        try { moduleAbstract = hit.getString("abstract");     } catch (JSONException e) {}
	        try { authorPauseId  = hit.getString("author");       } catch (JSONException e) {}
	        try { releaseName    = hit.getString("distribution"); } catch (JSONException e) {}
	        try { releaseVersion = hit.getString("version");      } catch (JSONException e) {}
	        
//	        Log.d("ModuleKeywordSearch", "name: " + name);
//	        Log.d("ModuleKeywordSearch", "abstract: " + moduleAbstract);
	        
	        Module module = getModel().acquireInstance(name);
	        module.setModuleAbstract(moduleAbstract);
	        module.setReleaseName(releaseName);
	        module.getRelease().setVersion(releaseVersion);
	        module.getRelease().setAuthorPauseId(authorPauseId);
	        
	        results.add(module);
        }

        if (results instanceof Results<?>)
        	((Results<?>) results).setTotalSize(response.getJSONObject("hits").getInt("total"));
    }
	
	@Override
	public String toString() {
		return getModel() + ":ModuleKeywordSearch(" + safeKeywords + ")";
	}
}
