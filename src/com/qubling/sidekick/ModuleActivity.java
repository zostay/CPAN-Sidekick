/*
 * Copyright 2011 Qubling Software, LLC.
 * 
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick;

import com.qubling.sidekick.metacpan.AuthorByDistributionSearch;
import com.qubling.sidekick.metacpan.FavoriteByDistributionSearch;
import com.qubling.sidekick.metacpan.HttpClientManager;
import com.qubling.sidekick.metacpan.ModuleFetcher;
import com.qubling.sidekick.metacpan.RatingByDistributionSearch;
import com.qubling.sidekick.metacpan.collection.DistributionList;
import com.qubling.sidekick.metacpan.collection.ModelList;
import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;

import android.app.Activity;
import android.util.Log;

public abstract class ModuleActivity extends Activity {
	
	protected abstract class ModuleFetchTask {
		public abstract void doFetchTask(HttpClientManager clientManager, Module module);
	}

	protected void fetchModule(final Module module, final ModuleList.OnModuleListUpdated listener, final ModuleFetchTask... fetchTasks) {
		
		// In this case we will fetchModule() twice, once to load the basic module info and 
		// then again afterwards to load the POD and other stuff that depends on the first 
		// bit of info
		if (module.isModuleFetchNeeded()) {
			HttpClientManager clientManager = new HttpClientManager(1);
			
			ModuleList moduleList = module.toModuleList();
			
			moduleList.addModelListUpdatedListener(new ModuleList.OnModuleListUpdated() {
				@Override
				public void onModelListUpdated(ModelList<Module> modelList) {
					if (listener != null) 
						listener.onModelListUpdated(modelList);
					
					fetchModule(module, listener, fetchTasks);
				}
			});
			
			new ModuleFetcher(clientManager, moduleList).execute();
			return;
		}
		
		int taskCount = fetchTasks.length;
		if (module.getAuthor().isGravatarURLNeeded() || module.getAuthor().isGravatarBitmapNeeded()) taskCount++;
		if (module.getDistribution().isFavoriteNeeded()) taskCount++;
		if (module.getDistribution().isRatingNeeded()) taskCount++;
		
		HttpClientManager clientManager = new HttpClientManager(taskCount);
		
		for (ModuleFetchTask fetchTask : fetchTasks) {
			fetchTask.doFetchTask(clientManager, module);
		}
		
		if (taskCount > fetchTasks.length) {
			ModuleList moduleList = module.toModuleList();
			
			if (listener != null)
				moduleList.addModelListUpdatedListener(listener);
			
			if (module.getAuthor().isGravatarURLNeeded() || module.getAuthor().isGravatarBitmapNeeded()) {
				Log.d("ModuleViewActivity", "isGravatarBitmapNeeded: true");
				new AuthorByDistributionSearch(clientManager, this, moduleList.extractAuthorList()).execute();
			}
			
			if (module.getDistribution().isFavoriteNeeded() || module.getDistribution().isRatingNeeded()) {
				DistributionList distributionList = moduleList.extractDistributionList();
				if (module.getDistribution().isFavoriteNeeded())
					new FavoriteByDistributionSearch(clientManager, this, distributionList).execute();
				
				if (module.getDistribution().isRatingNeeded())
					new RatingByDistributionSearch(clientManager, this, distributionList).execute();
			}
		}
		
	}

}
