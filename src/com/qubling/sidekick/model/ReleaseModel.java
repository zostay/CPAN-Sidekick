package com.qubling.sidekick.model;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qubling.sidekick.api.JSONFragment;
import com.qubling.sidekick.model.CPANQueryFetcher.SearchSection;

public class ReleaseModel extends Model<Release> {
	public ReleaseModel(Schema schema) {
		super(schema);
	}
	
	protected Release constructInstance(String name) {
		return new Release(this, name);
	}
	
	private JSONFragment makeReleasesTerms(final String prefix, final ResultSet<Release> releases) {
        JSONFragment releasesTerms = new JSONFragment() {

            @Override
            public String toJSONString() {
                JSONArray terms = new JSONArray();

                try {
                    for (Release release : releases) {

                        JSONObject favoriteDistribution = new JSONObject()
                                .put(prefix + ".distribution", release.getName());

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
        
        return releasesTerms;
	}
	
	public Fetcher<Release> fetchFavorites(final ResultSet<Release> releases, final String myPrivateToken) {
		final JSONFragment releasesTerms = makeReleasesTerms("favorite", releases);
		
		CPANQueryFetcher.SearchCallback<Release> favoritesCallback = new CPANQueryFetcher.SearchCallback<Release>() {
			@Override
			public void prepareRequest(Map<String, Object> variables) {
				variables.put("distributions", releasesTerms);
				variables.put("myPrivateToken", myPrivateToken);
			}
			
			@Override
	    	public void consumeResponse(JSONObject response, ResultSet<Release> newResults) throws JSONException {
		        JSONArray favorites = response.getJSONObject("facets").getJSONObject("favorites").getJSONArray("terms");
		        for (int i = 0; i < favorites.length(); i++) {
		            JSONObject favorite = favorites.getJSONObject(i);

		            String releaseName = favorite.getString("term");
		            int favoriteCount = favorite.getInt("count");

		            Release release = releases.get(releaseName);
		            if (release != null) {
		                release.setFavoriteCount(favoriteCount);
		            }
		            
		            newResults.add(release);
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
		            
		            newResults.add(release);
		        }
			}
		};
		
		CPANQueryFetcher<Release> favoritesByRelease = new CPANQueryFetcher<Release>(SearchSection.FAVORITE, "favorite_by_distribution", favoritesCallback);
		return favoritesByRelease;
	}
	
	public Fetcher<Release> fetchRatings(final ResultSet<Release> releases) {
		final JSONFragment releasesTerms = makeReleasesTerms("rating", releases);
		
		CPANQueryFetcher.SearchCallback<Release> ratingsCallback = new CPANQueryFetcher.SearchCallback<Release>() {
			@Override
			public void prepareRequest(Map<String, Object> variables) {
				variables.put("distributions", releasesTerms);
			}
			
			@Override
			public void consumeResponse(JSONObject response, ResultSet<Release> newResults) throws JSONException {
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
		};
		
		CPANQueryFetcher<Release> ratingsByRelease = new CPANQueryFetcher<Release>(SearchSection.RATING, "rating_by_distribution", ratingsCallback);
		return ratingsByRelease;
	}
}
