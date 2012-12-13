package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.Collections;

import com.qubling.sidekick.fetch.Fetcher;
import com.qubling.sidekick.fetch.SubqueryFetcher;
import com.qubling.sidekick.fetch.UpdateFetcher;
import com.qubling.sidekick.fetch.cpan.CPANQueryFetcher;
import com.qubling.sidekick.fetch.cpan.ModuleDetailsFetcher;
import com.qubling.sidekick.fetch.cpan.ModuleForReleaseFetcher;
import com.qubling.sidekick.fetch.cpan.ModuleKeywordSearch;
import com.qubling.sidekick.fetch.cpan.ModulePodFetcher;
import com.qubling.sidekick.instance.Author;
import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.instance.Release;
import com.qubling.sidekick.search.ResultSet;
import com.qubling.sidekick.search.Schema;

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
    
    public Fetcher<Module> fetchModulesForRelease(Release release) {
        CPANQueryFetcher<Module> fetcher = new ModuleForReleaseFetcher(this, release);
        fetcher.setSize(999);
        return fetcher;
    }
	
	public UpdateFetcher<Module> fetchPod() {
		return new ModulePodFetcher(this);
	}
	
	public UpdateFetcher<Module> fetch() {
		return new ModuleDetailsFetcher(this);
	}
	
	public UpdateFetcher<Module> fetchReleaseFavorites(String myPrivateToken) {
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
	
	@Override
	public String toString() {
		return getSchema() + ":ModuleModel";
	}
}
