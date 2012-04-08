/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.cpan.result;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Details about a single CPAN author.
 *
 * @author sterling
 *
 */
public class Author extends Model {

    private String pauseId;
    private String name;
    private boolean gravatarURLLoaded = false;
    private String gravatarURL;
    private Bitmap gravatarBitmap;

    public Author(String pauseId) {
        this.pauseId = pauseId;
    }

    public Author(Parcel in) {
        pauseId           = in.readString();
        name              = in.readString();
        gravatarURLLoaded = readParcelBoolean(in);
        gravatarURL       = in.readString();
        gravatarBitmap    = in.readParcelable(Author.class.getClassLoader());
    }

    @Override
    public String getPrimaryID() {
        return pauseId;
    }

    @Override
    public int hashCode() {
        return pauseId.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof Author) && ((Author) object).pauseId.equals(pauseId);
    }

    public String getPauseId() {
        return pauseId;
    }

    public void setPauseId(String authorPauseId) {
        this.pauseId = authorPauseId;
    }
    
    public String getName() {
    	return name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }

    public String getGravatarURL() {
        return gravatarURL;
    }

    public void setGravatarURL(String authorGravatarURL) {
        this.gravatarURL = authorGravatarURL;
        this.gravatarURLLoaded = true;
    }

    public Bitmap getGravatarBitmap() {
        return gravatarBitmap;
    }

    public void setGravatarBitmap(Bitmap authorGravatarBitmap) {
        this.gravatarBitmap = authorGravatarBitmap;
    }

    public boolean isGravatarURLNeeded() {
        return !gravatarURLLoaded;
    }

    public boolean isGravatarBitmapNeeded() {
        return this.gravatarURL != null && this.gravatarBitmap == null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pauseId);
        dest.writeString(name);
        writeParcelBoolean(dest, gravatarURLLoaded);
        dest.writeString(gravatarURL);
        dest.writeParcelable(gravatarBitmap, flags);
    }

    public static final Parcelable.Creator<Author> CREATOR
            = new Parcelable.Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };
}
