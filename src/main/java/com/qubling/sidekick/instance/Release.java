package com.qubling.sidekick.instance;

import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.search.Schema;

import android.os.Parcel;
import android.os.Parcelable;

public class Release extends Instance<Release> {
	private String name;
	private String version;
	private Author author;
	private int favoriteCount = -1;
	private boolean myFavorite;
	private long ratingCount = -1;
	private double ratingMean;
	
	private String license;
	
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
		license       = in.readString();
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
	
	public boolean hasFavoriteCount() {
		return favoriteCount >= 0;
	}

	public int getFavoriteCount() {
    	return favoriteCount < 0 ? 0 : favoriteCount;
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
	
	public boolean hasRatingCount() {
		return ratingCount >= 0;
	}

	public long getRatingCount() {
    	return ratingCount < 0 ? 0 : ratingCount;
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
	
	public String getLicense() {
	    return license;
	}
	
	public void setLicense(String license) {
	    this.license = license;
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
    	out.writeString(license);
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
    
    @Override
    public void attachRelatedModels(Schema schema) {
        if (author != null) {
            author.attachToModel(schema.getAuthorModel());
        }
    }
    
    @Override
    public void addToCache() {
        getModel().cache(this);
        if (author != null)
            author.addToCache();
    }
    
    @Override
    public String toString() {
    	return "Release(" + getName() + ")";
    }
}
