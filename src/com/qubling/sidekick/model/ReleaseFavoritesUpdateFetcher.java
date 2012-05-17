package com.qubling.sidekick.model;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qubling.sidekick.api.JSONFragment;

public class ReleaseFavoritesUpdateFetcher extends ReleaseUpdateFetcher {
	private String myPrivateToken;
	
	public ReleaseFavoritesUpdateFetcher(Model<Release> model, String myPrivateToken) {
		super(model, SearchSection.FAVORITE, "favorite_by_distribution");
	
		this.myPrivateToken = myPrivateToken;
	}
	
	@Override
	protected void prepareRequest(Map<String, Object> variables) {
		JSONFragment releasesTerms = makeReleasesTerms("favorite");
		
		variables.put("distributions", releasesTerms);
		variables.put("myPrivateToken", myPrivateToken);
	}
	
	@Override
	protected void consumeResponse(JSONObject response) throws JSONException {
		ResultSet<Release> releases = getResultSet();
		
        JSONArray favorites = response.getJSONObject("facets").getJSONObject("favorites").getJSONArray("terms");
        for (int i = 0; i < favorites.length(); i++) {
            JSONObject favorite = favorites.getJSONObject(i);

            String releaseName = favorite.getString("term");
            int favoriteCount = favorite.getInt("count");

            Release release = releases.get(releaseName);
            if (release != null) {
                release.setFavoriteCount(favoriteCount);
            }
        }

        JSONArray myFavorites = response.getJSONObject("facets").getJSONObject("myfavorites").getJSONArray("terms");
        for (int i = 0; i < myFavorites.length(); i++) {
            JSONObject favorite = myFavorites.getJSONObject(i);

            String releaseName = favorite.getString("term");
            //int favoriteCount = favorite.getInt("count");

            Release release = releases.get(releaseName);
            if (release != null) {
                release.setMyFavorite(true);
            }
        }
	}
}