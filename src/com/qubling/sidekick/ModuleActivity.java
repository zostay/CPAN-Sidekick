/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.qubling.sidekick.api.HttpClientManager;
import com.qubling.sidekick.cpan.result.Module;

/**
 * This is an abstract activity for sharing functionality between the
 * {@link ModuleSearchActivity} and the {@link ModuleViewActivity}.
 *
 * @author sterling
 *
 */
public abstract class ModuleActivity extends FragmentActivity implements HttpClientManager.OnHttpClientAction {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

	@Override
    public void onActionsStart() {
        setProgressBarIndeterminateVisibility(true);
    }

	@Override
    public void onActionsComplete() {
        setProgressBarIndeterminateVisibility(false);
    }
	
    public void startSearch(boolean modal) {}
    public void cancelSearch() {}
    protected void onModuleClick(Module clickedModule) {}
    
}
