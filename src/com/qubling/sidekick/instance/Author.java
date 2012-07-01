package com.qubling.sidekick.instance;

import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.search.Schema;

import android.os.Parcel;
import android.os.Parcelable;

public class Author extends Instance<Author> {
	private String pauseId;
	private Gravatar gravatar;
	
	public Author(Model<Author> model, String pauseId) {
		super(model);
		
		this.pauseId = pauseId;
	}
	
	public Author(Parcel in) {
		pauseId  = in.readString();
		gravatar = in.readParcelable(Author.class.getClassLoader());
	}

	@Override
	public String getKey() {
		return pauseId;
	}

	public String getPauseId() {
    	return pauseId;
    }

	public String getGravatarUrl() {
    	return gravatar == null ? null : gravatar.getUrl();
    }

	public void setGravatarUrl(String gravatarUrl) {
		gravatar = getModel().getSchema().getGravatarModel().acquireInstance(gravatarUrl);
    }
	
	public Gravatar getGravatar() {
    	return gravatar;
    }

	public void setGravatar(Gravatar gravatar) {
    	this.gravatar = gravatar;
    }
	
	@Override
	public int describeContents() {
		return 0;
	}

    @Override
    public void writeToParcel(Parcel out, int flags) {
    	out.writeString(pauseId);
    	out.writeParcelable(gravatar, flags);
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
    
    @Override
    protected void attachRelatedModels(Schema schema) {
        if (gravatar != null) {
            gravatar.attachToModel(schema.getGravatarModel());
        }
    }
    
    @Override
    protected void addToCache() {
        getModel().cache(this);
        if (gravatar != null)
            gravatar.addToCache();
    }
    
    @Override
    public String toString() {
    	return "Author(" + getPauseId() + ")";
    }
}
