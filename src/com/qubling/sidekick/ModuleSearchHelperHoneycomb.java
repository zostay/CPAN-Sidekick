package com.qubling.sidekick;

import android.app.SearchManager;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;

public class ModuleSearchHelperHoneycomb extends ModuleSearchHelper {
	public ModuleSearchHelperHoneycomb(ModuleSearchActivity activity) {
		super(activity);
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_module_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);
    	
    	return true;
    }
    
    @Override
    public boolean onSearchRequested() {
    	// TODO Make onSearchRequested() do something useful.
    	return false;
    }
}
