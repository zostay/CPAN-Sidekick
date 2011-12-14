package com.qubling.sidekick.metacpan.result;

import android.os.Parcel;
import android.os.Parcelable;

public class Distribution implements Parcelable {
	
	private String name;
	private String version;
	private int ratingCount;
	private double rating;
	
	private int favoriteCount;
	private boolean myFavorite;

	public Distribution(String name, String version) {
		this.name    = name;
		this.version = version;
	}
	
	public Distribution(Parcel in) {
		name          = in.readString();
		version       = in.readString();
		ratingCount   = in.readInt();
		rating        = in.readDouble();
		favoriteCount = in.readInt();
		myFavorite    = in.readByte() == 0 ? false : true;
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
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double distributionRating) {
		this.rating = distributionRating;
	}

	public int getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(int distributionFavoriteCount) {
		this.favoriteCount = distributionFavoriteCount;
	}

	public boolean isMyFavorite() {
		return myFavorite;
	}

	public void setMyFavorite(boolean distributionMyFavorite) {
		this.myFavorite = distributionMyFavorite;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(version);
		dest.writeInt(ratingCount);
		dest.writeDouble(rating);
		dest.writeInt(favoriteCount);
		dest.writeByte((byte) (myFavorite ? 1 : 0));
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
