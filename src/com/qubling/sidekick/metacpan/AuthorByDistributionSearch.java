/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.metacpan;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.qubling.sidekick.metacpan.collection.AuthorList;
import com.qubling.sidekick.metacpan.result.Author;

/**
 * This fetches the details for a group of authors, given the PAUSE ID for each.
 * 
 * @author sterling
 *
 */
public class AuthorByDistributionSearch extends MetaCPANSearch<Void> {

    private AuthorList authorList;
    private Map<String, Author> authorMap;
    private int countGravatarURLs = 0;

    public AuthorByDistributionSearch(HttpClientManager clientManager, Context context, AuthorList authorList) {
        super(clientManager, context, SearchSection.AUTHOR, "author_by_pauseid");

        this.authorList = authorList;
        this.authorMap  = new HashMap<String, Author>();

        this.setSize(0);
    }

    @Override
    public JSONObject search() {
        JSONFragment authors = new JSONFragment() {

            @Override
            public String toJSONString() {
                JSONArray terms = new JSONArray();

                try {
                    for (Author author : authorList) {
                        if (AuthorByDistributionSearch.this.authorMap.containsKey(author.getPauseId()))
                            continue;

                        Log.d("AuthorByDistributionSearch", "Adding Author: " + author.getPauseId());

                        AuthorByDistributionSearch.this.authorMap.put(author.getPauseId(), author);

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

        Map<String, Object> variables = buildVariables(
            "pauseids", authors
        );

        return makeMetaCPANRequest(variables);
    }

    @Override
    public Void constructCompiledResult(JSONObject results)
            throws JSONException {

        JSONArray hits = results.getJSONObject("hits").getJSONArray("hits");

        for (int i = 0; i < hits.length(); i++) {
            JSONObject jsonAuthor = hits.getJSONObject(i).getJSONObject("_source");

            String pauseId = jsonAuthor.getString("pauseid");
            String gravatarURL = jsonAuthor.getString("gravatar_url");

            Author author = authorMap.get(pauseId);
            if (author != null) {
                author.setGravatarURL(gravatarURL);
                countGravatarURLs++;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        HttpClientManager clientManager = new HttpClientManager(countGravatarURLs);
        for (Author author : authorList) {
            if (author.getGravatarURL() == null)
                continue;

            new GravatarFetcher(getContext(), clientManager, authorList).execute(author);
        }
    }
}
