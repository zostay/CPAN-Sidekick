package com.qubling.sidekick.ui.module;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.qubling.sidekick.R;
import com.qubling.sidekick.fetch.Fetcher;
import com.qubling.sidekick.fetch.UpdateFetcher;
import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.model.ModuleModel;
import com.qubling.sidekick.search.ResultSet;
import com.qubling.sidekick.search.Schema;
import com.qubling.sidekick.search.Search;
import com.qubling.sidekick.widget.ModuleListAdapter;

public class ModuleSearchFragment extends ModuleFragment implements Fetcher.OnFinished<Module> {
	
	private Search<Module> search;
    private String lastSearchText;

    private Schema searchSession;

    public Schema getSearchSession() {
    	return searchSession;
    }
    
    public ResultSet<Module> getResultSet() {
    	return search != null ? search.getResultSet() : null;
    }

    private ListView getSearchResultsListView() {
        ListView resultsView = (ListView) getActivity().findViewById(R.id.list_search_results);
        return resultsView;
    }

    public void onSearchCompleted(ModuleListAdapter adapter) {
    	((ModuleSearchActivity) getActivity()).onSearchCompleted(adapter);
    }

	@Override
    public void onCreate(Bundle state) {
	    super.onCreate(state);
	    
	    searchSession = new Schema(this.getActivity());
//        search = buildSearch("");

        // Load from the state, if we can
        if (state != null && state.containsKey("moduleList")) {
        	ArrayList<Module> moduleList = state.getParcelableArrayList("moduleList");
            lastSearchText = state.getString("lastSearchText");
            search = buildSearch(lastSearchText);
        	search.getResultSet().addAll(moduleList);
        	search.getResultSet().setTotalSize(state.getInt("moduleListTotalSize"));
        }
    }

	@Override
    public void onActivityCreated(Bundle state) {
	    super.onActivityCreated(state);

        ModuleListAdapter adapter = new ModuleListAdapter(this.getActivity(), search);
        ListView moduleListView = getSearchResultsListView();
        moduleListView.setAdapter(adapter);

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

        freshenModuleList();
    }
	
	private void freshenModuleList() {
		// TODO Do we need to itereate through the loaded modules and fetch
		// their details again just to be sure we have them?
	}
	
	private Search<Module> buildSearch(String searchText) {
		Log.d("ModuleSearchFragment", "buildSearch(" + searchText + ")");
		
		// Load the fetchers we need
        ModuleModel modules = getSearchSession().getModuleModel();
        Fetcher<Module> keywordSearch  = modules.searchByKeyword(lastSearchText = searchText);
        UpdateFetcher<Module> fetchFavorites = modules.fetchReleaseFavorites("");
        UpdateFetcher<Module> fetchRatings   = modules.fetchReleaseRatings();
        UpdateFetcher<Module> fetchAuthors   = modules.fetchAuthors();
        UpdateFetcher<Module> fetchGravatars = modules.fetchGravatars(GRAVATAR_DP_SIZE);
        
        // Start the search task
        @SuppressWarnings("unchecked")
        Search<Module> search = getSearchSession()
        		.doFetch(keywordSearch, this)
        		.thenDoFetch(
        				fetchAuthors.thenDoFetch(fetchGravatars),
        				fetchFavorites, 
        				fetchRatings
        			);

        ListView moduleListView = getSearchResultsListView();
        if (moduleListView != null) {
        	ModuleListAdapter adapter = (ModuleListAdapter) moduleListView.getAdapter();
        	adapter.setSearch(search);
        }
        
        search.addOnSearchActivityListener(this.getModuleActivity());
		
        return search;
	}

	public void doNewSearch(String searchText) {
        
        // Start the search task
        search = buildSearch(searchText);
        
        startSearch(true);
        
        ListView moduleListView = getSearchResultsListView();
        ModuleListAdapter moduleListAdapter = new ModuleListAdapter(
        		getActivity(), search);
        moduleListView.setAdapter(moduleListAdapter);
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

	    ArrayList<Module> moduleList = getResultSet().toArrayList();
        state.putParcelableArrayList("moduleList", moduleList);
        state.putInt("moduleListTotalCount", getResultSet().getTotalSize());
        state.putString("lastSearchText", lastSearchText);

        // Remember which one has been tapped
        ModuleListAdapter adapter = (ModuleListAdapter) getSearchResultsListView().getAdapter();
        int position = adapter.getCurrentModule();

        state.putInt("moduleListCurrentSelection", position);
    }

	@Override
    public void onFinishedFetch(Fetcher<Module> fetcher, ResultSet<Module> results) {
		Log.d("ModuleSearchFragment", "onFinishedFetch()");
		redrawModuleList();
    }

    private void redrawModuleList() {
    	// Might happen if the results are still loading when the screen is rotated or something
    	if (getActivity() == null) return;

        // Load the module list
    	ListView moduleSearchResults = (ListView) getActivity().findViewById(R.id.list_search_results);
    	ModuleListAdapter adapter = (ModuleListAdapter) moduleSearchResults.getAdapter();

        onSearchCompleted(adapter);
        
        adapter.notifyDataSetChanged();

        cancelSearch();
    }

    private synchronized void startSearch(boolean modal) {
        getModuleActivity().startSearch(modal);
        Log.d("ModuleSearchFragment", "startSearch(" + modal + ")");
    	search.start();
    }

    private synchronized void cancelSearch() {
//    	searchSession.cancelSearch();
        getModuleActivity().cancelSearch();
    }
}
