package com.qubling.sidekick.metacpan.result;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Author implements Parcelable {
	
	private String pauseId;
	private String gravatarURL;
	private Bitmap gravatarBitmap;

	public Author(String pauseId) {
		this.pauseId = pauseId;
	}
	
	public Author(Parcel in) {
		pauseId        = in.readString();
		gravatarURL    = in.readString();
		gravatarBitmap = in.readParcelable(Author.class.getClassLoader());
	}

	public String getPauseId() {
		return pauseId;
	}

	public void setPauseId(String authorPauseId) {
		this.pauseId = authorPauseId;
	}

	public String getGravatarURL() {
		return gravatarURL;
	}

	public void setGravatarURL(String authorGravatarURL) {
		this.gravatarURL = authorGravatarURL;
	}

	public Bitmap getGravatarBitmap() {
		return gravatarBitmap;
	}

	public void setGravatarBitmap(Bitmap authorGravatarBitmap) {
		this.gravatarBitmap = authorGravatarBitmap;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(pauseId);
		dest.writeString(gravatarURL);
		dest.writeParcelable(gravatarBitmap, flags);
	}

    public static final Parcelable.Creator<Author> CREATOR
            = new Parcelable.Creator<Author>() {
        public Author createFromParcel(Parcel in) {
        	return new Author(in);
        }

        public Author[] newArray(int size) {
            return new Author[size];
        }
    };
}
