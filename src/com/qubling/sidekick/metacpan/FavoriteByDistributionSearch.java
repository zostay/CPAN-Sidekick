package com.qubling.sidekick.metacpan;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;

public class FavoriteByDistributionSearch extends MetaCPANSearch<Void> {
	
	private ModuleList moduleList;
	private Map<String, Module> distributionMap;
	
	public FavoriteByDistributionSearch(HttpClientManager clientManager, Context context, ModuleList moduleList, Map<String, Module> distributionMap) {
		super(clientManager, context, SearchSection.FAVORITE, "favorite_by_distribution");
		
		this.moduleList      = moduleList;
		this.distributionMap = distributionMap;
		
		this.setSize(0);
	}

	@Override
	public JSONObject search() {
		JSONFragment distributions = new JSONFragment() {
			
			@Override
			public String toJSONString() {
				JSONArray terms = new JSONArray();
				
				try {
					for (Module module : FavoriteByDistributionSearch.this.distributionMap.values()) {
						
						JSONObject favoriteDistribution = new JSONObject()
								.put("favorite.distribution", module.getDistributionName());
						
						JSONObject term = new JSONObject()
								.put("term", favoriteDistribution);

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
			"distributions",  distributions,
			"myPrivateToken", ""
		);
		
		return makeMetaCPANRequest(variables);
	}

	@Override
	public Void constructCompiledResult(JSONObject results) throws JSONException {
		
		JSONArray favorites = results.getJSONObject("facets").getJSONObject("favorites").getJSONArray("terms");
		for (int i = 0; i < favorites.length(); i++) {
			JSONObject favorite = favorites.getJSONObject(i);
			
			String distributionName = favorite.getString("term"); 
			int favoriteCount = favorite.getInt("count");
			
			Module module = distributionMap.get(distributionName);
			if (module != null) {
				module.setDistributionFavoriteCount(favoriteCount);
			}
		}
		
		JSONArray myFavorites = results.getJSONObject("facets").getJSONObject("myfavorites").getJSONArray("terms");
		for (int i = 0; i < myFavorites.length(); i++) {
			JSONObject favorite = myFavorites.getJSONObject(i);
			
			String distributionName = favorite.getString("term"); 
			//int favoriteCount = favorite.getInt("count");
			
			Module module = distributionMap.get(distributionName);
			if (module != null) {
				module.setDistributionMyFavorite(true);
			}
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		moduleList.notifyModelListUpdated();
	}
}
