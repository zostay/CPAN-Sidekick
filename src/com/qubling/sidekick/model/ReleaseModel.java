package com.qubling.sidekick.model;

public class ReleaseModel extends Model<Release> {
	public ReleaseModel(Schema schema) {
		super(schema);
	}
	
	protected Release constructInstance(String name) {
		return new Release(this, name);
	}
	
	public UpdateFetcher<Release> fetchFavorites(String myPrivateToken) {
		return new ReleaseFavoritesUpdateFetcher(this, myPrivateToken);
	}
	
	public UpdateFetcher<Release> fetchRatings() {
		return new ReleaseRatingsUpdateFetcher(this);
	}
}
