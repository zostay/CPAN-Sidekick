package com.qubling.sidekick;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public abstract class ModuleSearchHelper {
    public static ModuleSearchHelper createInstance(ModuleSearchActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new ModuleSearchHelperHoneycomb(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
            return new ModuleSearchHelperEclair(activity);
        } else {
            throw new RuntimeException("CPAN Sidekick does not support versions of Android before 2.1.");
        }
    }

    private ModuleSearchActivity activity;

    public ModuleSearchHelper(ModuleSearchActivity activity) {
    	this.activity = activity;
    }

    public void setActivity(ModuleSearchActivity activity) {
    	this.activity = activity;
    }

    public ModuleSearchActivity getActivity() {
    	return activity;
    }

    public void onCreate(Bundle state) {
    }

    public Boolean onSearchRequested() {
    	return null;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getActivity().getMenuInflater();
    	inflater.inflate(R.menu.module_search, menu);

    	return true;
    }
}
