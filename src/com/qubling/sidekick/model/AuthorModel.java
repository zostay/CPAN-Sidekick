package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.Collections;

import com.qubling.sidekick.fetch.AuthorDetailsFetcher;
import com.qubling.sidekick.fetch.SubqueryFetcher;
import com.qubling.sidekick.fetch.UpdateFetcher;

public class AuthorModel extends Model<Author> {
	
	public AuthorModel(Schema schema) {
		super(schema);
	}
	
	protected Author constructInstance(String pauseId) {
		return new Author(this, pauseId);
	}

	public UpdateFetcher<Author> fetchDetails() {
		return new AuthorDetailsFetcher(this);
	}
	
	public UpdateFetcher<Author> fetchGravatars(float gravatarDpSize) {
		Results.Remap<Author, Gravatar> remapper = new Results.Remap<Author, Gravatar>() {
			@Override
			public Collection<Gravatar> map(Author author) {
				if (author.getGravatar() == null) {
					return Collections.emptyList();
				}
				else {
					return Collections.singleton(author.getGravatar());
				}
			}
		};
		UpdateFetcher<Gravatar> fetcher = getSchema().getGravatarModel().fetch(gravatarDpSize);
		
		return new SubqueryFetcher<Author, Gravatar>(this, fetcher, remapper);
	}
	
	@Override
	public String toString() {
		return getSchema() + ":AuthorModel";
	}
}
