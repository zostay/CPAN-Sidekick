package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import com.qubling.sidekick.model.CPANDirectFetcher.FetchSection;
import com.qubling.sidekick.model.CPANQueryFetcher.SearchSection;

public class ModuleModel extends Model<Module> {
	public ModuleModel(Schema schema) {
		super(schema);
	}
	
	protected Module constructInstance(String name) {
		return new Module(this, name);
	}
	
	public Fetcher<Module> searchByKeyword(String keywords) {
		final String safeKeywords = keywords.replace("\"", "\\\"");
		final String cleanKeywords = safeKeywords.replace("::", " ");
		
		CPANQueryFetcher.SearchCallback<Module> keywordSearchCallbacks = new CPANQueryFetcher.SearchCallback<Module>() {
			@Override
			public void prepareRequest(Map<String, Object> variables) {
				variables.put("query", safeKeywords);
				variables.put("cleanQuery", cleanKeywords);
			}
			
			@Override
            public void consumeResponse(JSONObject response, ResultSet<Module> results) throws JSONException {

		        // Slurp up the matches
		        JSONArray hits = response.getJSONObject("hits").getJSONArray("hits");
		        for (int i = 0; i < hits.length(); i++) {
		            JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");
		            
			        String name = null;
			        try {
			            if (hit.has("module")) {
			                name = hit.getJSONArray("module").getJSONObject(0).getString("name");
			            }
			            else {
			                name = hit.getString("name");
			            }
			        }
			        catch (JSONException e) {
			            name = "Unknown Module Name";
			        }

			        String moduleAbstract = null;
			        String authorPauseId  = null;
			        String releaseName    = null;
			        String releaseVersion = null;

			        try { moduleAbstract = hit.getString("abstract");     } catch (JSONException e) {}
			        try { authorPauseId  = hit.getString("author");       } catch (JSONException e) {}
			        try { releaseName    = hit.getString("distribution"); } catch (JSONException e) {}
			        try { releaseVersion = hit.getString("version");      } catch (JSONException e) {}
			        
			        Module module = acquireInstance(name);
			        module.setAbstract(moduleAbstract);
			        module.setReleaseName(releaseName);
			        module.getRelease().setVersion(releaseVersion);
			        module.getRelease().setAuthorPauseId(authorPauseId);
			        
			        results.add(module);
		        }

		        results.setTotalSize(response.getJSONObject("hits").getInt("total"));
            }
		};
		
		CPANQueryFetcher<Module> keywordModuleFetcher = new CPANQueryFetcher<Module>(SearchSection.FILE, "module_search", keywordSearchCallbacks);
		return keywordModuleFetcher;
	}
	
	public Fetcher<Module> fetchPod(final Module module) {
		CPANDirectFetcher.FetchCallback<Module> podCallbacks = new CPANDirectFetcher.FetchCallback<Module>() {
			@Override
            public void consumeResponse(String content, ResultSet<Module> results) {
				module.setRawPod(content);
				results.add(module);
            }
		};
		
		CPANDirectFetcher<Module> podFetcher = new CPANDirectFetcher<Module>(FetchSection.MODULE_POD, module.getName(), podCallbacks);
		return podFetcher;
	}
	
	public Fetcher<Module> fetch(final Module module) {
		CPANDirectFetcher.FetchCallback<Module> fetchCallbacks = new CPANDirectFetcher.FetchCallback<Module>() {
			@Override
			public void consumeResponse(String content, ResultSet<Module> results) {
				try {
		            Object parsedContent = new JSONTokener(content).nextValue();
		            if (parsedContent instanceof JSONObject) {
		                JSONObject json = (JSONObject) parsedContent;
	
		                // Basic Module info
		                module.setAbstract(json.getString("abstract"));
	
		                // Basic Distribution Info
		                module.setReleaseName(json.getString("distribution"));
		                module.getRelease().setVersion(json.getString("version"));
	
		                // Basic Author Info
		                module.getRelease().setAuthorPauseId(json.getString("author"));
		                
		                results.add(module);
		            }
		            else {
		                // TODO Show an alert dialog or toast when this happens
		                Log.e("ModuleModel", "Unexpected JSON content: " + parsedContent);
		            }
				}
				catch (JSONException e) {
					// TODO Show an alert dialog or toast when this happens
					Log.e("ModuleModel", "Error reading JSON response while fetching details: " + e.getMessage(), e);
				}
			}
		};
		
		CPANDirectFetcher<Module> detailsFetcher = new CPANDirectFetcher<Module>(FetchSection.MODULE_FETCH, module.getName(), fetchCallbacks);
		return detailsFetcher;
	}
	
	public Fetcher<Module> fetchReleaseFavorites(final ResultSet<Module> modules, String myPrivateToken) {
		ResultSet<Release> releases = new ResultSet<Release>();
		releases.addRemap(modules, new ResultSet.Remap<Module, Release>() {
			@Override
			public Collection<Release> map(Module module) {
				Release release = module.getRelease();
				if (release == null) {
					return Collections.emptyList();
				}
				else {
					return Collections.singleton(release);
				}
			}
		});
		
		Fetcher<Release> fetcher = getSchema().getReleaseModel().fetchFavorites(releases, myPrivateToken);
		return new SubqueryFetcher<Module, Release>(fetcher, null);
	}
	
	public Fetcher<Module> fetchReleaseRatings(final ResultSet<Module> modules) {
		ResultSet<Release> releases = new ResultSet<Release>();
		releases.addRemap(modules, new ResultSet.Remap<Module, Release>() {
			@Override
			public Collection<Release> map(Module module) {
				Release release = module.getRelease();
				if (release == null) {
					return Collections.emptyList();
				}
				else {
					return Collections.singleton(release);
				}
			}
		});
		
		Fetcher<Release> fetcher = getSchema().getReleaseModel().fetchRatings(releases);
		return new SubqueryFetcher<Module, Release>(fetcher, null);
	}
	
	public Fetcher<Module> fetchAuthors(final ResultSet<Module> modules) {
		ResultSet<Author> authors = new ResultSet<Author>();
		authors.addRemap(modules, new ResultSet.Remap<Module, Author>() {
			@Override
			public Collection<Author> map(Module module) {
				Author author = module.getAuthor();
				if (author == null) {
					return Collections.emptyList();
				}
				else {
					return Collections.singleton(author);
				}
			}
		});
		
		Fetcher<Author> fetcher = getSchema().getAuthorModel().fetch(authors);
		return new SubqueryFetcher<Module, Author>(fetcher, null);
	}
	
	public Fetcher<Module> fetchGravatars(final ResultSet<Module> modules, float gravatarDpSize) {
		ResultSet<Author> authors = new ResultSet<Author>();
		authors.addRemap(modules, new ResultSet.Remap<Module, Author>() {
			@Override
			public Collection<Author> map(Module module) {
				Author author = module.getAuthor();
				if (author == null) {
					return Collections.emptyList();
				}
				else {
					return Collections.singleton(author);
				}
			}
		});
		
		Fetcher<Author> fetcher = getSchema().getAuthorModel().fetchGravatars(authors, gravatarDpSize);
		return new SubqueryFetcher<Module, Author>(fetcher, null);
	}
}
