/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.api.cpan;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.qubling.sidekick.api.HttpClientManager;
import com.qubling.sidekick.api.JSONFragment;
import com.qubling.sidekick.cpan.collection.DistributionList;
import com.qubling.sidekick.cpan.result.Distribution;

/**
 * This fetches the favorites for a list of distributions.
 *
 * @author sterling
 *
 */
public class FavoriteByDistributionSearch extends MetaCPANSearch<Void> {

    private DistributionList distributionList;
    private Map<String, Distribution> distributionMap;

    public FavoriteByDistributionSearch(HttpClientManager clientManager, Context context, DistributionList distributionList, Map<String, Distribution> distributionMap) {
        super(clientManager, context, SearchSection.FAVORITE, "favorite_by_distribution");

        this.distributionList = distributionList;
        this.distributionMap  = distributionMap;

        this.setSize(0);
    }

    public FavoriteByDistributionSearch(HttpClientManager clientManager, Context context, DistributionList distributionList) {
        super(clientManager, context, SearchSection.FAVORITE, "favorite_by_distribution");

        this.distributionList = distributionList;
        this.distributionMap  = new HashMap<String, Distribution>();

        this.setSize(0);

        for (Distribution distribution : distributionList) {
            this.distributionMap.put(distribution.getName(), distribution);
        }
    }

    @Override
    public JSONObject search() {
        JSONFragment distributions = new JSONFragment() {

            @Override
            public String toJSONString() {
                JSONArray terms = new JSONArray();

                try {
                    for (Distribution distribution : FavoriteByDistributionSearch.this.distributionMap.values()) {

                        JSONObject favoriteDistribution = new JSONObject()
                                .put("favorite.distribution", distribution.getName());

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

            Distribution distribution = distributionMap.get(distributionName);
            if (distribution != null) {
                distribution.setFavoriteCount(favoriteCount);
            }
        }

        JSONArray myFavorites = results.getJSONObject("facets").getJSONObject("myfavorites").getJSONArray("terms");
        for (int i = 0; i < myFavorites.length(); i++) {
            JSONObject favorite = myFavorites.getJSONObject(i);

            String distributionName = favorite.getString("term");
            //int favoriteCount = favorite.getInt("count");

            Distribution distribution = distributionMap.get(distributionName);
            if (distribution != null) {
                distribution.setMyFavorite(true);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        distributionList.notifyModelListUpdated();

        super.onPostExecute(result);
    }
}
