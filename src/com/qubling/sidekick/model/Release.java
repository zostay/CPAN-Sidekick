package com.qubling.sidekick.model;

public class Release extends Instance<Release> {
	private String name;
	private String version;
	private Author author;
	private int favoriteCount;
	private boolean myFavorite;
	private long ratingCount;
	private double ratingMean;
	
	public Release(Model<Release> model, String name) {
		super(model);
		
		this.name = name;
	}
	
	@Override
	public String getKey() {
		return name;
	}

	public String getName() {
    	return name;
    }
	
	public String getVersion() {
    	return version;
    }

	public void setVersion(String version) {
    	this.version = version;
    }

	public String getAuthorPauseId() {
		return author == null ? null : author.getPauseId();
	}
	
	public void setAuthorPauseId(String pauseId) {
		author = getModel().getSchema().getAuthorModel().acquireInstance(pauseId);
	}

	public Author getAuthor() {
    	return author;
    }

	public int getFavoriteCount() {
    	return favoriteCount;
    }

	public void setFavoriteCount(int favoriteCount) {
    	this.favoriteCount = favoriteCount;
    }

	public boolean isMyFavorite() {
    	return myFavorite;
    }

	public void setMyFavorite(boolean myFavorite) {
    	this.myFavorite = myFavorite;
    }

	public long getRatingCount() {
    	return ratingCount;
    }

	public void setRatingCount(long ratingCount) {
    	this.ratingCount = ratingCount;
    }

	public double getRatingMean() {
    	return ratingMean;
    }

	public void setRatingMean(double ratingMean) {
    	this.ratingMean = ratingMean;
    }
}
