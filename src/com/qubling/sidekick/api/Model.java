/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.api;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Base class for data models.
 *
 * @author sterling
 *
 */
public abstract class Model implements Parcelable {
	
    public interface OnModelChanged {
        public void onModelChange(Model changedModel);
    }

    private OnModelChanged onModelChangedListener;

    public void setOnModelChangedListener(OnModelChanged listener) {
        onModelChangedListener = listener;
    }

    public void notifyModelChanged() {
        if (onModelChangedListener == null)
            onModelChangedListener.onModelChange(this);
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

    public abstract Object getPrimaryID();
}
