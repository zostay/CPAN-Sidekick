package com.qubling.sidekick.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qubling.sidekick.api.JSONFragment;

public abstract class ReleaseUpdateFetcher extends
        CPANQueryUpdateFetcher<Release> {

	public ReleaseUpdateFetcher(Model<Release> model, SearchSection searchSection, String searchTemplate) {
	    super(model, searchSection, searchTemplate);
    }

	protected JSONFragment makeReleasesTerms(final String prefix) {
		final ResultSet<Release> releases = getResultSet();
		
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

}
