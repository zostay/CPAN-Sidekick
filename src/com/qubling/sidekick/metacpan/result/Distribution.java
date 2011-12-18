package com.qubling.sidekick.metacpan.result;

import android.os.Parcel;
import android.os.Parcelable;

public class Distribution extends Model {
	
	private String name;
	private String version;
	
	private boolean ratingLoaded = false;
	private int ratingCount;
	private double rating;
	
	private boolean favoriteLoaded = false;
	private int favoriteCount;
	private boolean myFavorite;
	

	public Distribution(String name, String version) {
		this.name    = name;
		this.version = version;
	}
	
	public Distribution(Parcel in) {
		name           = in.readString();
		version        = in.readString();
		ratingLoaded   = readParcelBoolean(in);
		ratingCount    = in.readInt();
		rating         = in.readDouble();
		favoriteLoaded = readParcelBoolean(in);
		favoriteCount  = in.readInt();
		myFavorite     = readParcelBoolean(in);
	}
	
	public int hashCode() {
		return name.hashCode() ^ version.hashCode();
	}

	public String getName() {
		return name;
	}

	public void setName(String distributionName) {
		this.name = distributionName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String distributionVersion) {
		this.version = distributionVersion;
	}

	public int getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(int distributionRatingCount) {
		this.ratingCount = distributionRatingCount;
		ratingLoaded = true;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double distributionRating) {
		this.rating = distributionRating;
		ratingLoaded = true;
	}

	public int getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(int distributionFavoriteCount) {
		this.favoriteCount = distributionFavoriteCount;
		favoriteLoaded = true;
	}

	public boolean isMyFavorite() {
		return myFavorite;
	}

	public void setMyFavorite(boolean distributionMyFavorite) {
		this.myFavorite = distributionMyFavorite;
		favoriteLoaded = true;
	}
	
	public boolean isRatingNeeded() {
		return !ratingLoaded;
	}
	
	public boolean isFavoriteNeeded() {
		return !favoriteLoaded;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(version);
		writeParcelBoolean(dest, ratingLoaded);
		dest.writeInt(ratingCount);
		dest.writeDouble(rating);
		writeParcelBoolean(dest, favoriteLoaded);
		dest.writeInt(favoriteCount);
		writeParcelBoolean(dest, myFavorite);
	}
	
    public static final Parcelable.Creator<Distribution> CREATOR
            = new Parcelable.Creator<Distribution>() {
        public Distribution createFromParcel(Parcel in) {
        	return new Distribution(in);
        }

        public Distribution[] newArray(int size) {
            return new Distribution[size];
        }
    };

}
