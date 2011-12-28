/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.qubling.sidekick.api.HttpClientManager;
import com.qubling.sidekick.api.cpan.AuthorByDistributionSearch;
import com.qubling.sidekick.api.cpan.FavoriteByDistributionSearch;
import com.qubling.sidekick.api.cpan.ModuleFetcher;
import com.qubling.sidekick.api.cpan.RatingByDistributionSearch;
import com.qubling.sidekick.cpan.collection.DistributionList;
import com.qubling.sidekick.cpan.collection.ModelList;
import com.qubling.sidekick.cpan.collection.ModuleList;
import com.qubling.sidekick.cpan.result.Module;

/**
 * This is an abstract activity for sharing functionality between the
 * {@link ModuleSearchActivity} and the {@link ModuleViewActivity}.
 *
 * @author sterling
 *
 */
public abstract class ModuleActivity extends Activity implements HttpClientManager.OnHttpClientAction {

    protected abstract class ModuleFetchTask {
        public abstract void doFetchTask(HttpClientManager clientManager, Module module);
    }

    // Never access this directly! Always use getClientManager()
	private HttpClientManager clientManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

	protected void fetchModule(final Module module, final ModuleList.OnModuleListUpdated listener, final ModuleFetchTask... fetchTasks) {

        // In this case we will fetchModule() twice, once to load the basic module info and
        // then again afterwards to load the POD and other stuff that depends on the first
        // bit of info
        if (module.isModuleFetchNeeded()) {
            ModuleList moduleList = module.toModuleList();

            moduleList.addModelListUpdatedListener(new ModuleList.OnModuleListUpdated() {
                @Override
                public void onModelListUpdated(ModelList<Module> modelList) {
                    if (listener != null)
                        listener.onModelListUpdated(modelList);

                    fetchModule(module, listener, fetchTasks);
                }
            });

            new ModuleFetcher(getClientManager(), moduleList).execute();
            return;
        }

        int taskCount = fetchTasks.length;
        if (module.getAuthor().isGravatarURLNeeded() || module.getAuthor().isGravatarBitmapNeeded()) taskCount++;
        if (module.getDistribution().isFavoriteNeeded()) taskCount++;
        if (module.getDistribution().isRatingNeeded()) taskCount++;

        for (ModuleFetchTask fetchTask : fetchTasks) {
            fetchTask.doFetchTask(getClientManager(), module);
        }

        if (taskCount > fetchTasks.length) {
            ModuleList moduleList = module.toModuleList();

            if (listener != null)
                moduleList.addModelListUpdatedListener(listener);

            if (module.getAuthor().isGravatarURLNeeded() || module.getAuthor().isGravatarBitmapNeeded()) {
//                Log.d("ModuleViewActivity", "isGravatarBitmapNeeded: true");
                new AuthorByDistributionSearch(getClientManager(), this, moduleList.extractAuthorList()).execute();
            }

            if (module.getDistribution().isFavoriteNeeded() || module.getDistribution().isRatingNeeded()) {
                DistributionList distributionList = moduleList.extractDistributionList();
                if (module.getDistribution().isFavoriteNeeded())
                    new FavoriteByDistributionSearch(getClientManager(), this, distributionList).execute();

                if (module.getDistribution().isRatingNeeded())
                    new RatingByDistributionSearch(getClientManager(), this, distributionList).execute();
            }
        }

    }

	protected HttpClientManager getClientManager() {
    	if (clientManager == null) {
    		clientManager = new HttpClientManager();
    		clientManager.addOnHttpClientActionListener(this);
    	}
    	
    	return clientManager;
    }

	@Override
    public void onActionsStart() {
        setProgressBarIndeterminateVisibility(true);
    }

	@Override
    public void onActionsComplete() {
        setProgressBarIndeterminateVisibility(false);
    }

}
