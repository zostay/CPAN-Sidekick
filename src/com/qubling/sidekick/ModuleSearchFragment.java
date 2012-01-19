package com.qubling.sidekick;

import com.qubling.sidekick.api.cpan.ModuleSearch;
import com.qubling.sidekick.cpan.collection.ModelList;
import com.qubling.sidekick.cpan.collection.ModuleList;
import com.qubling.sidekick.cpan.result.Module;
import com.qubling.sidekick.widget.ModuleListAdapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ModuleSearchFragment extends ModuleFragment implements ModuleList.OnModuleListUpdated, ModuleList.OnMoreItemsRequested {

    private ModuleList moduleList;
    private String lastSearchText;    
    
    private ModuleSearch currentSearch;
    
    private boolean searchRunning = false;
    
    public ListView onSearchCompleted(ModuleListAdapter adapter) {
    	((ModuleSearchActivity) getActivity()).onSearchCompleted();

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
            }

        });
        
        final EditText queryText = (EditText) getView().findViewById(R.id.text_search);

        final ImageButton searchButton = (ImageButton) getView().findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View searchButton) {

                    // Hide the screen keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchButton.getWindowToken(), 0);

                    // Clear the module list
                    moduleList.clear();

                    // Start the search task
                    ModuleSearch search = new ModuleSearch(
                    		getClientManager(),
                            ModuleSearchFragment.this.getActivity(),
                            moduleList,
                            lastSearchText = queryText.getText().toString());
                    startSearch(search, true);
            }
        });

        queryText.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // Support KEYCODE_NUMPAD_ENTER added in API 11
                int KEYCODE_NUMPAD_ENTER = KeyEvent.KEYCODE_UNKNOWN;
                try {
                    KEYCODE_NUMPAD_ENTER = KeyEvent.class.getField("KEYCODE_NUMPAD_ENTER").getInt(null);
                }
                catch (Throwable t) {
                    // ignore
                }

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KEYCODE_NUMPAD_ENTER) {
                    if (!searchRunning)
                        searchButton.performClick();
                    return true;
                }

                return false;
            }
        });
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
        if (moduleList != list) {
            moduleList = list;
            moduleList.addModelListUpdatedListener(this);
            moduleList.addMoreItemsRequestedListener(this);
//            Log.d("ModuleSearchActivity", "moduleList.size(): " + moduleList.size());

            // Show search results
            ModuleListAdapter adapter = new ModuleListAdapter(getActivity(), list);
            onSearchCompleted(adapter);
        }

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
