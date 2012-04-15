/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.cpan.result;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.qubling.sidekick.api.Model;

/**
 * Details regarding a single CPAN distribution (a.k.a. release).
 *
 * @author sterling
 *
 */
public class Distribution extends Model {

    private String name;
    private String version;
    
    private Author author;
    
    // Other metadata
    private Date updated;
    private String status;
    private String maturity;
    private boolean authorized;
    private String license;

    private boolean ratingLoaded = false;
    private int ratingCount;
    private double rating;

    private boolean favoriteLoaded = false;
    private int favoriteCount;
    private boolean myFavorite;


    public Distribution(String name, String version, Author author) {
        this.name    = name;
        this.version = version;
        this.author  = author;
    }

    public Distribution(Parcel in) {
        name           = in.readString();
        version        = in.readString();
        
        author         = in.readParcelable(Distribution.class.getClassLoader());
        
        updated        = readParcelDate(in);
        status         = in.readString();
        maturity       = in.readString();
        authorized     = readParcelBoolean(in);
        license        = in.readString();
        
        ratingLoaded   = readParcelBoolean(in);
        ratingCount    = in.readInt();
        rating         = in.readDouble();
        favoriteLoaded = readParcelBoolean(in);
        favoriteCount  = in.readInt();
        myFavorite     = readParcelBoolean(in);
    }

    @Override
    public String getPrimaryID() {
        return makePrimaryID(name, version);
    }

    public static String makePrimaryID(String name, String version) {
        return name + "-" + version;
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ version.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof Distribution) && ((Distribution) object).name.equals(name)
                                                && ((Distribution) object).version.equals(version);
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

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Date getUpdated() {
    	return updated;
    }

	public void setUpdated(Date updated) {
    	this.updated = updated;
    }

	public String getStatus() {
    	return status;
    }

	public void setStatus(String status) {
    	this.status = status;
    }

	public String getMaturity() {
    	return maturity;
    }

	public void setMaturity(String maturity) {
    	this.maturity = maturity;
    }

	public boolean isAuthorized() {
    	return authorized;
    }

	public void setAuthorized(boolean authorized) {
    	this.authorized = authorized;
    }

	public String getLicense() {
    	return license;
    }

	public void setLicense(String license) {
    	this.license = license;
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
        dest.writeParcelable(author, flags);
        writeParcelDate(dest, updated);
        dest.writeString(status);
        dest.writeString(maturity);
        writeParcelBoolean(dest, authorized);
        dest.writeString(license);
        writeParcelBoolean(dest, ratingLoaded);
        dest.writeInt(ratingCount);
        dest.writeDouble(rating);
        writeParcelBoolean(dest, favoriteLoaded);
        dest.writeInt(favoriteCount);
        writeParcelBoolean(dest, myFavorite);
    }

    public static final Parcelable.Creator<Distribution> CREATOR
            = new Parcelable.Creator<Distribution>() {
        @Override
        public Distribution createFromParcel(Parcel in) {
            return new Distribution(in);
        }

        @Override
        public Distribution[] newArray(int size) {
            return new Distribution[size];
        }
    };

}
