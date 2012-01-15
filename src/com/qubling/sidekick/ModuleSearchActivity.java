/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.bugsense.trace.BugSenseHandler;

/**
 * An activity for searching for CPAN modules.
 * 
 * @author sterling
 *
 */
public class ModuleSearchActivity extends ModuleActivity {

    private ProgressDialog progressDialog;
    
    public void onSearchCompleted() {
        progressDialog.dismiss();
        progressDialog = null;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Setup the view
        setContentView(R.layout.module_search);
        
        // Setup BugSense
        BugSenseHandler.setup(this, Util.BUGSENSE_API_KEY);
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

    @Override
    public synchronized void cancelSearch() {

        // Clear the modal progress dialog if it is going
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}