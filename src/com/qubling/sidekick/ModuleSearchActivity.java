/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.Menu;

import com.bugsense.trace.BugSenseHandler;
import com.qubling.sidekick.cpan.result.Module;

/**
 * An activity for searching for CPAN modules.
 * 
 * @author sterling
 *
 */
public class ModuleSearchActivity extends ModuleActivity {
    final ModuleSearchHelper moduleSearchHelper = ModuleSearchHelper.createInstance(this);

    private ProgressDialog progressDialog;
    
    public void onSearchCompleted() {
    	if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    }
    
    private ModuleSearchFragment getModuleSearchFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return (ModuleSearchFragment) fragmentManager.findFragmentById(R.id.module_search_fragment);
    }
    
    private ModuleViewFragment getModuleViewFragment() {
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	return (ModuleViewFragment) fragmentManager.findFragmentById(R.id.module_view_fragment);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Setup the view
        setContentView(R.layout.module_search);
        
        // Setup BugSense
        BugSenseHandler.setup(this, Util.BUGSENSE_API_KEY);
        
        // Check to see if we got a search
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	String query = intent.getStringExtra(SearchManager.QUERY);
            getModuleSearchFragment().doNewSearch(query);
        }
        
        moduleSearchHelper.onCreate(state);
    }

    /**
     * Called when your activity's options menu needs to be created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	return moduleSearchHelper.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onSearchRequested() {
    	return moduleSearchHelper.onSearchRequested();
    }

	@Override
    public synchronized void startSearch(boolean modal) {
    	
    	// If modal, show the progress bar dialog
        if (modal) {
            String searchingCPAN = getResources().getString(R.string.dialog_searching_cpan);
            progressDialog = ProgressDialog.show(ModuleSearchActivity.this, "", searchingCPAN, true);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    cancelSearch();
                }
            });
        }
    	
    }
	
	public void doNewSearch(String searchText) {
		getModuleSearchFragment().doNewSearch(searchText);
	}

    @Override
    public synchronized void cancelSearch() {

        // Clear the modal progress dialog if it is going
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    
    @Override
    protected void onModuleClick(Module currentModule) {
    	ModuleViewFragment fragment = getModuleViewFragment();
        
        // Tablet
        if (fragment != null) {
        	fragment.setModule(currentModule);
        	fragment.fetchModule();
        }
        
        // Phone
        else {
	        Intent moduleViewIntent = new Intent(this, ModuleViewActivity.class);
	        moduleViewIntent.putExtra(ModuleViewActivity.EXTRA_MODULE, currentModule);
	        startActivity(moduleViewIntent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	ModuleViewFragment fragment = getModuleViewFragment();
    	
    	if (fragment == null)
    		return super.onKeyDown(keyCode, event);
    	
    	boolean result = fragment.onKeyDown(keyCode, event);
    	if (result) {
    		return result;
    	}
    	else {
    		return super.onKeyDown(keyCode, event);
    	}
    }
}