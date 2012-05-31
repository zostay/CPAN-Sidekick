package com.qubling.sidekick.fetch;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.qubling.sidekick.api.JSONFragment;
import com.qubling.sidekick.fetch.CPANQueryFetcher.SearchSection;
import com.qubling.sidekick.model.Author;
import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.model.ResultSet;

public class AuthorDetailsFetcher extends CPANQueryUpdateFetcher<Author> {
	
	public AuthorDetailsFetcher(Model<Author> model) {
	    super(model, SearchSection.AUTHOR, "author_by_pauseid");
    }
	
	@Override
	public boolean needsUpdate(Author author) {
		Log.d("AuthorDetailsFetcher", "author " + author.getPauseId() + " needs update? " + author.getGravatarUrl());
		return author.getGravatarUrl() == null;
	}

	private JSONFragment makeAuthorsTerms() {
		final ResultSet<Author> authors = getResultSet();
		
        return new JSONFragment() {

            @Override
            public String toJSONString() {
                JSONArray terms = new JSONArray();

                try {
                    for (Author author : authors) {

                        Log.d("AuthorDetailsFetcher", "Adding Author: " + author.getPauseId());

                        JSONObject pauseid = new JSONObject()
                                .put("pauseid", author.getPauseId());

                        JSONObject term = new JSONObject()
                                .put("term", pauseid);

                        terms.put(term);
                    }
                }
                catch (JSONException e) {
                    throw new RuntimeException("error while building JSON", e);
                }

                return terms.toString();
            }
        };
	}

	@Override
	protected void prepareRequest(Map<String, Object> variables) {
		variables.put("pauseids", makeAuthorsTerms());
	}

	@Override
	protected void consumeResponse(JSONObject response)
	        throws JSONException {
		
		ResultSet<Author> authors = getResultSet();
		
		JSONObject topHits = response.getJSONObject("hits");
		if (topHits == null) {
			Log.e("AuthorDetailsFetcher", "Unexpected response (top hits missing): " + response);
			return;
		}
		
        JSONArray hits = topHits.getJSONArray("hits");
        if (hits == null) {
        	Log.e("AuthorDetailsFetcher", "Unexpected response (nested hits missing): " + response);
        	return;
        }
		
		Log.d("AuthorDetailsFetcher", response.toString());

        for (int i = 0; i < hits.length(); i++) {
            JSONObject jsonAuthor = hits.getJSONObject(i).getJSONObject("_source");

            String pauseId = jsonAuthor.getString("pauseid");
            String gravatarUrl = jsonAuthor.getString("gravatar_url");

            Author author = authors.get(pauseId);
            if (author != null) {
            	author.setGravatarUrl(gravatarUrl);
            }
        }
	}

	@Override
	public String toString() {
		return getModel() + ":AuthorDetailsFetcher(" + getResultSet() + ")";
	}
}
