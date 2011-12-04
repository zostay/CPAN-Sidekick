package com.qubling.sidekick;

import java.util.ArrayList;

import com.qubling.sidekick.metacpan.ModuleList;
import com.qubling.sidekick.metacpan.ModuleSearch;
import com.qubling.sidekick.metacpan.ModuleSearchAdapter;
import com.qubling.sidekick.metacpan.result.Module;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class ModuleSearchActivity extends Activity implements ModuleList.OnModuleListUpdated {
	
	private ModuleList moduleList;
	private ProgressDialog progressDialog;
	
	public void lockOrientation() {
		int currentOrientation = getResources().getConfiguration().orientation;
		switch (currentOrientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		} 
	}
	
	public void unlockOrientation() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
	public void onSearchCompleted(ModuleSearchAdapter adapter) {
		progressDialog.cancel();
		
		ListView resultsView = (ListView) findViewById(R.id.list_search_results);
        resultsView.setAdapter(adapter);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_search);
        
        moduleList = new ModuleList();
        moduleList.setModuleListUpdater(this);
        
        final EditText queryText = (EditText) findViewById(R.id.text_search);
        
        final ImageButton searchButton = (ImageButton) findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View searchButton) {
				
				// Hide the screen keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(searchButton.getWindowToken(), 0);
				
				// Clear the module list
				moduleList.clear();
				
				// Show the progress bar
				progressDialog = ProgressDialog.show(ModuleSearchActivity.this, "", "Searching CPAN...", true);
				
				// Lock the orientation - prevents the activity from being paused in the middle of the search
				lockOrientation();
				
				// Start the search task
				new ModuleSearch(
						ModuleSearchActivity.this, 
						moduleList, 
						queryText.getText().toString()).execute();
			}
		});
        
        queryText.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_NUMPAD_ENTER:
					searchButton.performClick();
					return true;
				}
				return false;
			}
		});
    }
    
    @Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		
		Module[] modules = (Module[]) state.getParcelableArray("moduleList");
		moduleList = new ModuleList(modules);
		onModuleListUpdate(moduleList);
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		
		Module[] modules = new Module[moduleList.size()];
		state.putParcelableArray("moduleList", moduleList.toArray(modules));
	}

	public void onModuleListUpdate(ModuleList list) {
    	
    	// Show search results
    	ModuleSearchAdapter adapter = list.toModuleSearchAdapter(this);
    	ListView moduleListView = (ListView) findViewById(R.id.list_search_results);
    	moduleListView.setAdapter(adapter);
    	
    	// Dismiss the progress dialog
    	if (progressDialog != null) progressDialog.cancel();
    	
    	// Unlock the screen orientation
    	unlockOrientation();
    }
}