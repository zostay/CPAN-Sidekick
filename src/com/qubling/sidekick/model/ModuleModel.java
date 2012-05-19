package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.Collections;

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
	
	public UpdateFetcher<Module> fetchPod() {
		return new ModulePodFetcher(this);
	}
	
	public UpdateFetcher<Module> fetch() {
		return new ModuleDetailsFetcher(this);
	}
	
	public UpdateFetcher<Module> fetchReleaseFavorites(String myPrivateToken) {
		Results.Remap<Module, Release> remapper = new Results.Remap<Module, Release>() {
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
		Results.Remap<Module, Release> remapper = new Results.Remap<Module, Release>() {
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
		Results.Remap<Module, Author> remapper = new Results.Remap<Module, Author>() {
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
		Results.Remap<Module, Author> remapper = new Results.Remap<Module, Author>() {
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
