package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qubling.sidekick.api.JSONFragment;
import com.qubling.sidekick.model.CPANQueryFetcher.SearchSection;

public class AuthorModel extends Model<Author> {
	
	public AuthorModel(Schema schema) {
		super(schema);
	}
	
	protected Author constructInstance(String pauseId) {
		return new Author(this, pauseId);
	}

	public Fetcher<Author> fetch(final ResultSet<Author> authors) {		
        final JSONFragment authorsTerms = new JSONFragment() {

            @Override
            public String toJSONString() {
                JSONArray terms = new JSONArray();

                try {
                    for (Author author : authors) {

//                        Log.d("AuthorByDistributionSearch", "Adding Author: " + author.getPauseId());

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
		
		CPANQueryFetcher.SearchCallback<Author> fetchCallbacks = new CPANQueryFetcher.SearchCallback<Author>() {
			@Override
			public void prepareRequest(Map<String, Object> variables) {
				variables.put("pauseids", authorsTerms);
			}
			
			@Override
	    	public void consumeResponse(JSONObject response, ResultSet<Author> newAuthors) throws JSONException {
		        JSONArray hits = response.getJSONObject("hits").getJSONArray("hits");

		        for (int i = 0; i < hits.length(); i++) {
		            JSONObject jsonAuthor = hits.getJSONObject(i).getJSONObject("_source");

		            String pauseId = jsonAuthor.getString("pauseid");
		            String gravatarUrl = jsonAuthor.getString("gravatar_url");

		            Author author = authors.get(pauseId);
		            if (author != null) {
		                author.setGravatarUrl(gravatarUrl);
		            }
		            
		            newAuthors.add(author);
		        }
			}
		};
		
		CPANQueryFetcher<Author> authorByPauseIdFetcher = new CPANQueryFetcher<Author>(SearchSection.AUTHOR, "author_by_pauseid", fetchCallbacks);
		return authorByPauseIdFetcher;
	}
	
	public Fetcher<Author> fetchGravatars(final ResultSet<Author> authors, float gravatarDpSize) {
		ResultSet<Gravatar> gravatars = new ResultSet<Gravatar>();
		gravatars.addRemap(authors, new ResultSet.Remap<Author, Gravatar>() {
			@Override
			public Collection<Gravatar> map(Author author) {
				if (author.getGravatar() == null) {
					return Collections.emptyList();
				}
				else {
					return Collections.singleton(author.getGravatar());
				}
			}
		});
		Fetcher<Gravatar> fetcher = getSchema().getGravatarModel().fetch(gravatars, gravatarDpSize);
		
		return new SubqueryFetcher<Author, Gravatar>(fetcher, null);
	}
}
