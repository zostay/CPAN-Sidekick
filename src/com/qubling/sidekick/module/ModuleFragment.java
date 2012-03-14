package com.qubling.sidekick.module;

import android.support.v4.app.Fragment;

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
 * Base class for all module search/view fragments. These fragments must always
 * be used within a {@link ModuleActivity}.
 *
 * @author sterling
 */
public class ModuleFragment extends Fragment {

    protected abstract class ModuleFetchTask {
        public abstract void doFetchTask(HttpClientManager clientManager, Module module);
    }

    // Never access this directly! Always use getClientManager()
	private HttpClientManager clientManager;

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
                new AuthorByDistributionSearch(getClientManager(), getActivity(), moduleList.extractAuthorList()).execute();
            }

            if (module.getDistribution().isFavoriteNeeded() || module.getDistribution().isRatingNeeded()) {
                DistributionList distributionList = moduleList.extractDistributionList();
                if (module.getDistribution().isFavoriteNeeded())
                    new FavoriteByDistributionSearch(getClientManager(), getActivity(), distributionList).execute();

                if (module.getDistribution().isRatingNeeded())
                    new RatingByDistributionSearch(getClientManager(), getActivity(), distributionList).execute();
            }
        }

    }

	protected HttpClientManager getClientManager() {
    	if (clientManager == null) {
    		clientManager = new HttpClientManager();
    		clientManager.addOnHttpClientActionListener(getModuleActivity());
    	}

    	return clientManager;
    }

    protected ModuleActivity getModuleActivity() {
    	return (ModuleActivity) getActivity();
    }
}
