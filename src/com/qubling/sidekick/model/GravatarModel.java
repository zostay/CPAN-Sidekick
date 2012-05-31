package com.qubling.sidekick.model;

import com.qubling.sidekick.fetch.UpdateFetcher;
import com.qubling.sidekick.fetch.other.GravatarFetcher;

public class GravatarModel extends Model<Gravatar> {
	
	public GravatarModel(Schema schema) {
		super(schema);
	}
	
	protected Gravatar constructInstance(String url) {
		return new Gravatar(this, url);
	}

	public UpdateFetcher<Gravatar> fetch(float gravatarDpSize) {
		return new GravatarFetcher(this, gravatarDpSize);
	}
	
	@Override
	public String toString() {
		return getSchema() + ":GravatarModel";
	}
}
