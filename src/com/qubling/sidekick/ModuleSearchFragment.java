package com.qubling.sidekick;

import com.qubling.sidekick.api.cpan.ModuleSearch;
import com.qubling.sidekick.cpan.collection.ModelList;
import com.qubling.sidekick.cpan.collection.ModuleList;
import com.qubling.sidekick.cpan.result.Module;
import com.qubling.sidekick.widget.ModuleListAdapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ModuleSearchFragment extends ModuleFragment implements ModuleList.OnModuleListUpdated, ModuleList.OnMoreItemsRequested {

    private ModuleList moduleList;
    private String lastSearchText;    
    
    private ModuleSearch currentSearch;
    
    public ModuleSearch getCurrentSearch() {
    	return currentSearch;
    }
    
    public ListView onSearchCompleted(ModuleListAdapter adapter) {
    	((ModuleSearchActivity) getActivity()).onSearchCompleted(adapter);

        ListView resultsView = (ListView) getActivity().findViewById(R.id.list_search_results);
        resultsView.setAdapter(adapter);
        
        return resultsView;
    }
    
	@Override
    public void onCreate(Bundle state) {
	    super.onCreate(state);

        // Load from the state, if we can
        if (state != null && state.containsKey("moduleList")) {
            Parcelable[] moduleParcels = state.getParcelableArray("moduleList");
            int totalCount = state.getInt("moduleListTotalCount");
            lastSearchText = state.getString("lastSearchText");

            // Is this necessary? Had a class cast exception at one point.
            Module[] modules = new Module[moduleParcels.length];
            System.arraycopy(moduleParcels, 0, modules, 0, moduleParcels.length);

            moduleList = new ModuleList(modules, totalCount);

            moduleList.addModelListUpdatedListener(this);
            moduleList.addMoreItemsRequestedListener(this);

            onModelListUpdated(moduleList);
            freshenModuleList();
        }

        // Or load from scratch
        else {
            moduleList = new ModuleList();

            moduleList.addModelListUpdatedListener(this);
            moduleList.addMoreItemsRequestedListener(this);
        }
    }

	@Override
    public void onActivityCreated(Bundle state) {
	    super.onActivityCreated(state);

        ModuleListAdapter adapter = new ModuleListAdapter(this.getActivity(), moduleList);
        ListView moduleListView = onSearchCompleted(adapter);
        
        moduleListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View row, int position, long id) {
                ListView moduleListView = (ListView) parent;
                Module currentModule = (Module) moduleListView.getItemAtPosition(position);

                // This happens when you click on the progress throbber item
                if (currentModule == null)
                    return;

                getModuleActivity().onModuleClick(currentModule);
                
                ModuleListAdapter adapter = (ModuleListAdapter) parent.getAdapter();
                adapter.setCurrentModule(position);
            }

        });
    }
	
	public void doNewSearch(String searchText) {
        // Clear the module list
        moduleList.clear();

        // Start the search task
        ModuleSearch search = new ModuleSearch(
        		getClientManager(),
                ModuleSearchFragment.this.getActivity(),
                moduleList,
                lastSearchText = searchText);
        startSearch(search, true);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflater.inflate(R.layout.module_search_fragment, container, false);
	}

	@Override
    public void onSaveInstanceState(Bundle state) {
	    super.onSaveInstanceState(state);
	    
	    // If running, it should stop now...
	    cancelSearch();

        Module[] modules = new Module[moduleList.size()];
        state.putParcelableArray("moduleList", moduleList.toArray(modules));
        state.putInt("moduleListTotalCount", moduleList.getTotalCount());
        state.putString("lastSearchText", lastSearchText);
    }

    @Override
    public void onModelListUpdated(ModelList<Module> modelList) {
        ModuleList list = (ModuleList) modelList;
//        Log.d("ModuleSearchActivity", "onModelListUpdated");

        // Load the module list if this is a change in the underlying model
        ModuleListAdapter adapter;
        if (moduleList != list) {
            moduleList = list;
            moduleList.addModelListUpdatedListener(this);
            moduleList.addMoreItemsRequestedListener(this);
//            Log.d("ModuleSearchActivity", "moduleList.size(): " + moduleList.size());

            // Show search results
            adapter = new ModuleListAdapter(getActivity(), list);
        }
        
        else {
        	ListView moduleSearchResults = (ListView) getActivity().findViewById(R.id.list_search_results);
        	adapter = (ModuleListAdapter) moduleSearchResults.getAdapter();
        }
        
        onSearchCompleted(adapter);
        
        cancelSearch();
    }

    @Override
    public void onMoreItemsRequested(ModuleList list) {

        // Start the search task
        ModuleSearch search = new ModuleSearch(getClientManager(), getActivity(), moduleList, lastSearchText);
        search.setFrom(moduleList.size());

        startSearch(search, false);
    }

    private void freshenModuleList() {
        for (Module module : moduleList) {
            fetchModule(module, new ModuleList.OnModuleListUpdated() {

                @Override
                public void onModelListUpdated(ModelList<Module> modelList) {
                    moduleList.notifyModelListUpdated();
                }
            });
        }
    }

    private synchronized void startSearch(ModuleSearch newSearch, boolean modal) {
        getModuleActivity().startSearch(modal);
    	
    	currentSearch = newSearch;
        currentSearch.execute();
    }

    private synchronized void cancelSearch() {

        // Stop the search right now
        if (currentSearch != null) {
            currentSearch.cancel(false);
            currentSearch = null;
        }
        
        getModuleActivity().cancelSearch();
    }
}
