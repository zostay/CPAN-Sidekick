package com.qubling.sidekick.instance;

import com.qubling.sidekick.model.Model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Gravatar extends Instance<Gravatar> {
	private String url;
	private Bitmap bitmap;
	
	public Gravatar(Model<Gravatar> model, String url) {
		super(model);
		
		this.url    = url;
	}
	
	public Gravatar(Parcel in) {
		url    = in.readString();
		bitmap = in.readParcelable(Gravatar.class.getClassLoader());
	}
	
	public String getKey() {
		return url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

    @Override
    public void writeToParcel(Parcel out, int flags) {
    	out.writeString(url);
    	out.writeParcelable(bitmap, flags);
    }

    public static final Parcelable.Creator<Gravatar> CREATOR
            = new Parcelable.Creator<Gravatar>() {
        @Override
        public Gravatar createFromParcel(Parcel in) {
            return new Gravatar(in);
        }

        @Override
        public Gravatar[] newArray(int size) {
            return new Gravatar[size];
        }
    };
    
    @Override
    public String toString() {
    	return "Gravatar(" + getUrl() + ")";
    }
}
