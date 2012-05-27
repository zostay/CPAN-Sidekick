package com.qubling.sidekick.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Module extends Instance<Module> {
	private String name;
	private String moduleAbstract; // keywords clashing with variable names? tsk, tsk... #NeedPerlSigils
	private String rawPod;
	private Release release;
	
	public Module(Model<Module> model, String name) {
		super(model);
		
		this.name = name;
	}
	
	public Module(Parcel in) {
		name           = in.readString();
		moduleAbstract = in.readString();
		rawPod         = in.readString();
		release        = in.readParcelable(Module.class.getClassLoader());
	}
	
	@Override
	public String getKey() {
		return name;
	}

	public String getName() {
    	return name;
    }
	
	public String getAbstract() {
		return moduleAbstract;
	}
	
	public void setAbstract(String moduleAbstract) {
		this.moduleAbstract = moduleAbstract;
	}
	
	public String getRawPod() {
    	return rawPod;
    }

	public void setRawPod(String rawPod) {
    	this.rawPod = rawPod;
    }

	public String getReleaseName() {
		return release == null ? null : release.getName();
	}
	
	public void setReleaseName(String name) {
		release = getModel().getSchema().getReleaseModel().acquireInstance(name);
	}

	public Release getRelease() {
    	return release;
    }
	
	public String getAuthorPauseId() {
		return release == null ? null : release.getAuthorPauseId();
	}

	public Author getAuthor() {
    	return release == null ? null : release.getAuthor();
    }
	
	@Override
	public int describeContents() {
		return 0;
	}

    @Override
    public void writeToParcel(Parcel out, int flags) {
    	out.writeString(name);
    	out.writeString(moduleAbstract);
    	out.writeString(rawPod);
    	out.writeParcelable(release, flags);
    }

    public static final Parcelable.Creator<Module> CREATOR
            = new Parcelable.Creator<Module>() {
        @Override
        public Module createFromParcel(Parcel in) {
            return new Module(in);
        }

        @Override
        public Module[] newArray(int size) {
            return new Module[size];
        }
    };
    
    @Override
    public String toString() {
    	return "Module(" + getName() + ")";
    }
}
