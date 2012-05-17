package com.qubling.sidekick.model;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qubling.sidekick.api.JSONFragment;

public class ReleaseRatingsUpdateFetcher extends ReleaseUpdateFetcher implements
        UpdateFetcher<Release> {

	public ReleaseRatingsUpdateFetcher(Model<Release> model) {
		super(model, SearchSection.RATING, "rating_by_distribution");
	}

	@Override
	protected void prepareRequest(Map<String, Object> variables) {
		JSONFragment releasesTerms = makeReleasesTerms("rating");
		variables.put("distributions", releasesTerms);
	}

	@Override
	protected void consumeResponse(JSONObject response) throws JSONException {
		ResultSet<Release> releases = getResultSet();
		
        JSONArray facets = response.getJSONObject("facets").getJSONObject("ratings").getJSONArray("terms");
        
        for (int i = 0; i < facets.length(); i++) {
            JSONObject facet = facets.getJSONObject(i);

            String releaseName = facet.getString("term");
            int ratingCount = facet.getInt("count");
            double ratingMean = facet.getDouble("mean");

            Release release = releases.get(releaseName);
            if (release != null) {
                release.setRatingCount(ratingCount);
                release.setRatingMean(ratingMean);
            }
        }
	}
}
