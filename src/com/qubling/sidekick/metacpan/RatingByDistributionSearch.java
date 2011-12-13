package com.qubling.sidekick.metacpan;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;

public class RatingByDistributionSearch extends MetaCPANSearch<Void> {
	
	private ModuleList moduleList;
	private Map<String, Module> distributionMap;
	
	public RatingByDistributionSearch(HttpClientManager clientManager, Context context, ModuleList moduleList, Map<String, Module> distributionMap) {
		super(clientManager, context, SearchSection.RATING, "rating_by_distribution");
		
		this.moduleList      = moduleList;
		this.distributionMap = distributionMap;
		
		this.setSize(0);
	}

	public JSONObject search() {
		JSONFragment distributions = new JSONFragment() {
			
			@Override
			public String toJSONString() {
				JSONArray terms = new JSONArray();
				
				try {
					for (Module module : RatingByDistributionSearch.this.distributionMap.values()) {
						
						JSONObject ratingDistribution = new JSONObject()
								.put("rating.distribution", module.getDistributionName());
						
						JSONObject term = new JSONObject()
								.put("term", ratingDistribution);
						
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
			"distributions", distributions	
		);
		
		return makeMetaCPANRequest(variables);
	}
	
	public Void constructCompiledResult(JSONObject ratings) throws JSONException {
		JSONArray facets = ratings.getJSONObject("facets").getJSONObject("ratings").getJSONArray("terms");
		
		for (int i = 0; i < facets.length(); i++) {
			JSONObject facet = facets.getJSONObject(i);
			
			String distributionName = facet.getString("term"); 
			int ratingCount = facet.getInt("count");
			double ratingMean = facet.getDouble("mean");
			
			Module module = distributionMap.get(distributionName);
			if (module != null) {
				module.setDistributionRatingCount(ratingCount);
				module.setDistributionRating(ratingMean);
			}
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		moduleList.notifyModuleListUpdaters();
	}
}
