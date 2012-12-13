package com.qubling.sidekick.model;

import com.qubling.sidekick.fetch.UpdateFetcher;
import com.qubling.sidekick.fetch.cpan.ReleaseDetailsFetcher;
import com.qubling.sidekick.fetch.cpan.ReleaseFavoritesUpdateFetcher;
import com.qubling.sidekick.fetch.cpan.ReleaseRatingsUpdateFetcher;
import com.qubling.sidekick.instance.Release;
import com.qubling.sidekick.search.Schema;

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
    
    public UpdateFetcher<Release> fetch() {
        return new ReleaseDetailsFetcher(this);
    }
	
	@Override
	public String toString() {
		return getSchema() + ":ReleaseModel";
	}
}
