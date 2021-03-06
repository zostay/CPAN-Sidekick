package com.qubling.sidekick.ui.module;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import com.qubling.sidekick.R;

public abstract class ModuleSearchHelper {
    public static ModuleSearchHelper createInstance(SearchableActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new ModuleSearchHelperHoneycomb(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
            return new ModuleSearchHelperEclair(activity);
        } else {
            throw new RuntimeException("CPAN Sidekick does not support versions of Android before 2.1.");
        }
    }

    private SearchableActivity activity;

    public ModuleSearchHelper(SearchableActivity activity) {
    	this.activity = activity;
    }

    public void setActivity(SearchableActivity activity) {
    	this.activity = activity;
    }

    public SearchableActivity getActivity() {
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
