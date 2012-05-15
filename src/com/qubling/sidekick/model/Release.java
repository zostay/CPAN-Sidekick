package com.qubling.sidekick.model;

import android.os.Parcel;
import android.os.Parcelable;

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
	
	public Release(Parcel in) {
		name          = in.readString();
		version       = in.readString();
		author        = in.readParcelable(Release.class.getClassLoader());
		favoriteCount = in.readInt();
		myFavorite    = readParcelBoolean(in);
		ratingCount   = in.readLong();
		ratingMean    = in.readDouble();
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
	
	@Override
	public int describeContents() {
		return 0;
	}

    @Override
    public void writeToParcel(Parcel out, int flags) {
    	out.writeString(name);
    	out.writeString(version);
    	out.writeParcelable(author, flags);
    	out.writeInt(favoriteCount);
    	writeParcelBoolean(out, myFavorite);
    	out.writeLong(ratingCount);
    	out.writeDouble(ratingMean);
    }

    public static final Parcelable.Creator<Release> CREATOR
            = new Parcelable.Creator<Release>() {
        @Override
        public Release createFromParcel(Parcel in) {
            return new Release(in);
        }

        @Override
        public Release[] newArray(int size) {
            return new Release[size];
        }
    };
}
