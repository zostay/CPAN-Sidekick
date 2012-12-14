/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.ui.module;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.instance.Release;
import com.qubling.sidekick.search.Search.OnSearchActivity;
import com.qubling.sidekick.ui.release.ReleaseViewActivity;

/**
 * This is an abstract activity for sharing functionality between the
 * {@link ModuleSearchActivity} and the {@link ModuleViewActivity}.
 *
 * @author sterling
 *
 */
public abstract class ModuleActivity extends FragmentActivity implements OnSearchActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

	@Override
    public void onSearchStart() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
		        setProgressBarIndeterminateVisibility(true);
			}
		});
    }

	@Override
    public void onSearchComplete() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setProgressBarIndeterminateVisibility(false);
			}
		});
    }

    public void startSearch(boolean modal) {}
    public void cancelSearch() {}
    public void onModuleClick(Module clickedModule) {}
    
    public void onReleaseClick(Release clickedRelease) {
        Intent moduleReleaseIntent = new Intent(this, ReleaseViewActivity.class);
        moduleReleaseIntent.putExtra(ReleaseViewActivity.EXTRA_RELEASE, clickedRelease);
        startActivity(moduleReleaseIntent);
    }

}
