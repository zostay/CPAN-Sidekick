package com.qubling.sidekick.metacpan;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.qubling.sidekick.metacpan.collection.DistributionList;
import com.qubling.sidekick.metacpan.result.Distribution;

public class RatingByDistributionSearch extends MetaCPANSearch<Void> {
	
	private DistributionList distributionList;
	private Map<String, Distribution> distributionMap;
	
	public RatingByDistributionSearch(HttpClientManager clientManager, Context context, DistributionList distributionList, Map<String, Distribution> distributionMap) {
		super(clientManager, context, SearchSection.RATING, "rating_by_distribution");
		
		this.distributionList = distributionList;
		this.distributionMap  = distributionMap;
		
		this.setSize(0);
	}
	
	public RatingByDistributionSearch(HttpClientManager clientManager, Context context, Distribution distribution) {
		super(clientManager, context, SearchSection.RATING, "rating_by_distribution");
		
		this.distributionList = new DistributionList();
		this.distributionMap  = new HashMap<String, Distribution>();
		
		this.setSize(0);
		
		this.distributionList.add(distribution);
		this.distributionMap.put(distribution.getName(), distribution);
	}

	public JSONObject search() {
		JSONFragment distributions = new JSONFragment() {
			
			@Override
			public String toJSONString() {
				JSONArray terms = new JSONArray();
				
				try {
					for (Distribution distribution : RatingByDistributionSearch.this.distributionMap.values()) {
						
						JSONObject ratingDistribution = new JSONObject()
								.put("rating.distribution", distribution.getName());
						
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
			
			Distribution distribution = distributionMap.get(distributionName);
			if (distribution != null) {
				distribution.setRatingCount(ratingCount);
				distribution.setRating(ratingMean);
			}
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		distributionList.notifyModelListUpdated();
	}
}
