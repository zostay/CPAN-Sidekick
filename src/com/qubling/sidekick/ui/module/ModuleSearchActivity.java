/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.ui.module;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import com.qubling.sidekick.R;
import com.qubling.sidekick.Util;
import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.util.ConnectivityCheck;
import com.qubling.sidekick.widget.ModuleListAdapter;

/**
 * An activity for searching for CPAN modules.
 *
 * @author sterling
 *
 */
public class ModuleSearchActivity extends ModuleActivity {
    final ModuleSearchHelper moduleSearchHelper = ModuleSearchHelper.createInstance(this);

    private ProgressDialog progressDialog;

    public void onSearchCompleted(ModuleListAdapter adapter) {
    	if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}

    	// Show more help when we have results
    	ModuleViewPlaceholderFragment placeholderFragment = getModuleViewPlacholderFragment();
    	if (placeholderFragment != null) {
    		placeholderFragment.onSearchCompleted(adapter);
    	}
    }

    private ModuleSearchFragment getModuleSearchFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return (ModuleSearchFragment) fragmentManager.findFragmentById(R.id.module_search_fragment);
    }

    private boolean isTwoPanelView() {
    	View view = findViewById(R.id.module_view_fragment_container);
    	return view != null;
    }

    private ModuleViewPlaceholderFragment getModuleViewPlacholderFragment() {
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	ModuleViewThingyFragment fragment = (ModuleViewThingyFragment) fragmentManager.findFragmentById(R.id.module_view_fragment_container);

    	if (fragment instanceof ModuleViewPlaceholderFragment) {
    		return (ModuleViewPlaceholderFragment) fragment;
    	}
    	else {
    		return null;
    	}
    }

    private boolean isModuleViewFragmentAPlaceholder() {
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	ModuleViewThingyFragment fragment = (ModuleViewThingyFragment) fragmentManager.findFragmentById(R.id.module_view_fragment_container);
    	return fragment != null && fragment instanceof ModuleViewPlaceholderFragment;
    }

    private ModuleViewFragment getModuleViewFragment() {
    	if (isModuleViewFragmentAPlaceholder()) {
    		return null;
    	}
    	else {
    		FragmentManager fragmentManager = getSupportFragmentManager();
    		return (ModuleViewFragment) fragmentManager.findFragmentById(R.id.module_view_fragment_container);
    	}
    }

    private boolean convertToRealViewFragment() {
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	ModuleViewThingyFragment fragment = (ModuleViewThingyFragment) fragmentManager.findFragmentById(R.id.module_view_fragment_container);

    	if (fragment == null) return false;

    	// We do in fact have a placeholder to convert?
    	if (fragment instanceof ModuleViewPlaceholderFragment) {
    		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    		fragmentTransaction.replace(R.id.module_view_fragment_container, new ModuleViewFragment());

    		// Do not add to back stack. We don't want to go back to the placeholder
    		fragmentTransaction.commit();

    		// Go ahead and execute because we do this only immediately before showing the POD
    		// TODO There's probably a better, concurrent way of doing this
    		fragmentManager.executePendingTransactions();
    	}

    	return true;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Setup the view
        setContentView(R.layout.module_search);

        // Setup BugSense
        Util.setupBugSense(this);

        // Initialize the fragment, if on a tablet
        if (isTwoPanelView()) {
        	boolean showingModuleViewer = state != null
        			                   && state.getBoolean("showingModuleViewer");

        	Fragment viewFragment;
        	if (showingModuleViewer) {
        		viewFragment = new ModuleViewFragment();
        	}
        	else {
        		viewFragment = new ModuleViewPlaceholderFragment();
        	}

        	if(state == null)
	        	getSupportFragmentManager()
	        		.beginTransaction()
	        		.add(R.id.module_view_fragment_container, viewFragment)
	    			.commit();
        }

        // Check to see if we got a search
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	String query = intent.getStringExtra(SearchManager.QUERY);
            getModuleSearchFragment().doNewSearch(query);
        }

        moduleSearchHelper.onCreate(state);
        
        new ConnectivityCheck(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
	    super.onSaveInstanceState(state);

	    state.putBoolean("showingModuleViewer", !isModuleViewFragmentAPlaceholder());
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
    	Boolean result = moduleSearchHelper.onSearchRequested();
    	if (result == null) {
    		return super.onSearchRequested();
    	}
    	else {
    		return result;
    	}
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
    	boolean convertable = convertToRealViewFragment();
    	ModuleViewFragment fragment = getModuleViewFragment();

        // Tablet
        if (convertable) {
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