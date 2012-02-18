package com.qubling.sidekick;

import android.view.Menu;
import android.widget.SearchView;

public class ModuleSearchHelperHoneycomb extends ModuleSearchHelper {
	public ModuleSearchHelperHoneycomb(ModuleSearchActivity activity) {
		super(activity);
	}
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	SearchView moduleSearchView = (SearchView) menu.findItem(R.id.menu_module_search).getActionView();
    	
    	return true;
    }
    
    @Override
    public boolean onSearchRequested() {
    	return false;
    }
}
