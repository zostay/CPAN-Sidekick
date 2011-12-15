package com.qubling.sidekick.metacpan;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;

import android.content.Context;

public class ModuleSearch extends MetaCPANSearch<Module[]> {

	private String query;
	
	private ModuleList moduleList;
	private int totalCount = 0;
	
	public ModuleSearch(Context context, ModuleList list, String query) {
		super(new HttpClientManager(4), context, SearchSection.FILE, "module_search");

		this.query          = query.replace("\"", "\\\"");
		this.moduleList = list;
	}
	
	public JSONObject search() {
		
		// Setup the query
		String cleanQuery = query.replace("::", " ");
		
		Map<String, Object> variables = buildVariables(
			"query",      query,
			"cleanQuery", cleanQuery
		);
				
		return makeMetaCPANRequest(variables);
	}
	
	public Module[] constructCompiledResult(JSONObject searchResult) throws JSONException {
		
		// Slurp up the matches
		JSONArray hits = searchResult.getJSONObject("hits").getJSONArray("hits");
		int modulesCount = hits.length();
		Module[] modules = new Module[modulesCount];
		for (int i = 0; i < hits.length(); i++) {
			JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");
			
			modules[i] = Module.fromModuleSearch(hit);
		}
		
		totalCount = searchResult.getJSONObject("hits").getInt("total");
		
		return modules;
	}

	@Override
	protected void onPostExecute(Module[] modules) {
		super.onPostExecute(modules);
		
		// Nothing useful returned, forget it
		if (modules == null) return;
			
		// Stuff the matches into an adapter and fill the list
		Collections.addAll(moduleList, modules);
		moduleList.setTotalCount(totalCount);
		moduleList.notifyModelListUpdated();
		
		// Build a distributionMap, which will be reused
		Map<String, Module> distributionMap = new HashMap<String, Module>();
		for (Module module : moduleList) {
			if (distributionMap.containsKey(module.getDistributionName()))
				continue;
			distributionMap.put(module.getDistributionName(), module);
		}
		
		// Now, fire off the tasks that fill in the details
		new AuthorByDistributionSearch(getClientManager(), getContext(), moduleList.extractAuthorList()).execute();
		new FavoriteByDistributionSearch(getClientManager(), getContext(), moduleList, distributionMap).execute();
		new RatingByDistributionSearch(getClientManager(), getContext(), moduleList, distributionMap).execute();
	}

}
