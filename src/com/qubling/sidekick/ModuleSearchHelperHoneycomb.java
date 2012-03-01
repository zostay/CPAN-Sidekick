package com.qubling.sidekick;

import android.app.SearchManager;
import android.content.Context;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

public class ModuleSearchHelperHoneycomb extends ModuleSearchHelper {
	public ModuleSearchHelperHoneycomb(ModuleSearchActivity activity) {
		super(activity);
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.menu_module_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {		
			@Override
			public boolean onQueryTextSubmit(String query) {
				
				// Hide the screen keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                
                // Change the focus to avoid bringing the keyboard back up
                searchView.clearFocus();
                
				getActivity().doNewSearch(query);
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
    	
    	return true;
    }
}
