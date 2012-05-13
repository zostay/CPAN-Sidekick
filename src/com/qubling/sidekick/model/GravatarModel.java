package com.qubling.sidekick.model;

import java.util.ArrayList;

public class GravatarModel extends Model<Gravatar> {
	
	public GravatarModel(Schema schema) {
		super(schema);
	}
	
	protected Gravatar constructInstance(String url) {
		return new Gravatar(this, url);
	}

	public Fetcher<Gravatar> fetch(final ResultSet<Gravatar> gravatars, float gravatarDpSize) {
		ArrayList<String> gravatarUrls = new ArrayList<String>();
		for (Gravatar gravatar : gravatars) {
			gravatarUrls.add(gravatar.getUrl());
		}
		
		return new GravatarFetcher(gravatarUrls, gravatarDpSize);
	}
}
