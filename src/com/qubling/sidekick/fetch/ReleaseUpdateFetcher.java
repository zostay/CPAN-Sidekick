package com.qubling.sidekick.fetch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qubling.sidekick.api.JSONFragment;
import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.model.Release;
import com.qubling.sidekick.model.ResultSet;

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

	@Override
	public String toString() {
		return getModel() + ":RelaseUpdateFetcher(" + getResultSet() + ")";
	}
}
