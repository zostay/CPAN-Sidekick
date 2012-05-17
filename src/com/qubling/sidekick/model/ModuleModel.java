package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.Collections;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import com.qubling.sidekick.model.CPANDirectFetcher.FetchSection;

public class ModuleModel extends Model<Module> {
	public ModuleModel(Schema schema) {
		super(schema);
	}
	
	protected Module constructInstance(String name) {
		return new Module(this, name);
	}
	
	public Fetcher<Module> searchByKeyword(String keywords) {
		return new ModuleKeywordSearch(this, keywords);
	}
	
	public Fetcher<Module> fetchPod(final Module module) {
		CPANDirectFetcher.FetchCallback<Module> podCallbacks = new CPANDirectFetcher.FetchCallback<Module>() {
			@Override
            public void consumeResponse(String content, ResultSet<Module> results) {
				module.setRawPod(content);
				results.add(module);
            }
		};
		
		CPANDirectFetcher<Module> podFetcher = new CPANDirectFetcher<Module>(this, FetchSection.MODULE_POD, module.getName(), podCallbacks);
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
		
		CPANDirectFetcher<Module> detailsFetcher = new CPANDirectFetcher<Module>(this, FetchSection.MODULE_FETCH, module.getName(), fetchCallbacks);
		return detailsFetcher;
	}
	
	public Fetcher<Module> fetchReleaseFavorites(final ResultSet<Module> modules, String myPrivateToken) {
		ResultSet.Remap<Module, Release> remapper = new ResultSet.Remap<Module, Release>() {
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
		};
		
		UpdateFetcher<Release> fetcher = getSchema().getReleaseModel().fetchFavorites(myPrivateToken);
		return new SubqueryFetcher<Module, Release>(this, fetcher, remapper);
	}
	
	public UpdateFetcher<Module> fetchReleaseRatings() {
		ResultSet.Remap<Module, Release> remapper = new ResultSet.Remap<Module, Release>() {
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
		};
		
		UpdateFetcher<Release> fetcher = getSchema().getReleaseModel().fetchRatings();
		return new SubqueryFetcher<Module, Release>(this, fetcher, remapper);
	}
	
	public UpdateFetcher<Module> fetchAuthors() {
		ResultSet.Remap<Module, Author> remapper = new ResultSet.Remap<Module, Author>() {
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
		};
		
		UpdateFetcher<Author> fetcher = getSchema().getAuthorModel().fetchDetails();
		return new SubqueryFetcher<Module, Author>(this, fetcher, remapper);
	}
	
	public UpdateFetcher<Module> fetchGravatars(float gravatarDpSize) {
		ResultSet.Remap<Module, Author> remapper = new ResultSet.Remap<Module, Author>() {
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
		};
		
		UpdateFetcher<Author> fetcher = getSchema().getAuthorModel().fetchGravatars(gravatarDpSize);
		return new SubqueryFetcher<Module, Author>(this, fetcher, remapper);
	}
}
