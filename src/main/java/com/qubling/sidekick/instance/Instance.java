package com.qubling.sidekick.instance;

import java.util.Date;

import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.search.Schema;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class Instance<SomeInstance extends Instance<SomeInstance>> implements Parcelable {
	private transient Model<SomeInstance> model;
	
	public Instance() { }
	
	public Instance(Model<SomeInstance> model) {
		this.model = model;
	}
	
	public Model<SomeInstance> getModel() {
		if (isAttachedToModel()) {
			return model;
		}
		else {
			throw new IllegalStateException("You must call attachToModel() at your earliest convenience after construction.");
		}
	}
	
	public void attachToModel(Model<SomeInstance> model) {
		this.model = model;
		
		attachRelatedModels(model.getSchema());
		addToCache();
	}
	
	protected abstract void attachRelatedModels(Schema schema);
	protected abstract void addToCache();
	
	public boolean isAttachedToModel() {
		return this.model != null;
	}

	public abstract String getKey();

	@Override
    public boolean equals(Object o) {
		if (this.getClass().equals(o.getClass())) {
			return getKey().equals(((Instance<?>) o).getKey());
		}

		return false;
    }

	@Override
    public int hashCode() {
	    return getKey().hashCode();
    }

    protected static boolean readParcelBoolean(Parcel in) {
        return in.readByte() == 0 ? false : true;
    }

    protected static void writeParcelBoolean(Parcel dest, boolean value) {
        dest.writeByte((byte) (value ? 1 : 0));
    }
    
    protected static Date readParcelDate(Parcel in) {
    	Date result = null;
    	
    	long updatedMilliseconds = in.readLong();
        if (updatedMilliseconds >= 0) result =new Date(updatedMilliseconds);
    	
        return result;
    }
    
    protected static void writeParcelDate(Parcel dest, Date value) {
    	if (value == null) {
    		dest.writeLong(-1L);
    	}
    	else {
    		dest.writeLong(value.getTime());
    	}
    }
}
