package com.qubling.sidekick;

import com.qubling.sidekick.metacpan.ModuleSearch;
import com.qubling.sidekick.metacpan.collection.ModelList;
import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;
import com.qubling.sidekick.widget.ModuleListAdapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class ModuleSearchActivity extends ModuleActivity implements ModuleList.OnModuleListUpdated, ModuleList.OnMoreItemsRequested {
	
	private ModuleList moduleList;
	private ProgressDialog progressDialog;
	private String lastSearchText;
	
	public void onSearchCompleted(ModuleListAdapter adapter) {
		progressDialog.cancel();
		
		ListView resultsView = (ListView) findViewById(R.id.list_search_results);
        resultsView.setAdapter(adapter);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        
        // Setup the view
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.module_search);

        // Load from the state, if we can
        if (state != null && state.containsKey("moduleList")) {
        	Module[] modules = (Module[]) state.getParcelableArray("moduleList");
        	int totalCount = state.getInt("moduleListTotalCount");
        	lastSearchText = state.getString("lastSearchText");
		
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
		
		ModuleListAdapter adapter = new ModuleListAdapter(this, moduleList);
    	ListView moduleListView = (ListView) findViewById(R.id.list_search_results);
    	moduleListView.setAdapter(adapter);
        
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
				String searchingCPAN = getResources().getString(R.string.dialog_searching_cpan);
				progressDialog = ProgressDialog.show(ModuleSearchActivity.this, "", searchingCPAN, true);
				
				// Start the search task
				new ModuleSearch(
						ModuleSearchActivity.this, 
						moduleList, 
						lastSearchText = queryText.getText().toString()).execute();
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
					// TODO Probably need a better indicator that we've already started
					if (progressDialog == null)
						searchButton.performClick();
					return true;
				}
	        	
				return false;
			}
		});
        
        moduleListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View row, int position, long id) {
				ListView moduleListView = (ListView) parent;
				Module currentModule = (Module) moduleListView.getItemAtPosition(position);
				
				Intent moduleViewIntent = new Intent(ModuleSearchActivity.this, ModuleViewActivity.class);
				moduleViewIntent.putExtra(ModuleViewActivity.EXTRA_MODULE, currentModule);
				startActivity(moduleViewIntent);
			}
        	
		});
    }
    
    @Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		
		// Just make this go away, we won't need it...
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		
		Module[] modules = new Module[moduleList.size()];
		state.putParcelableArray("moduleList", moduleList.toArray(modules));
		state.putInt("moduleListTotalCount", moduleList.getTotalCount());
		state.putString("lastSearchText", lastSearchText);
	}

	public void onModelListUpdated(ModelList<Module> modelList) {
		ModuleList list = (ModuleList) modelList;
		Log.d("ModuleSearchActivity", "onModelListUpdated");
		
		// Load the module list if this is a change in the underlying model
		if (moduleList != list) {
			moduleList = list;
	        moduleList.addModelListUpdatedListener(this);
	        moduleList.addMoreItemsRequestedListener(this);
			Log.d("ModuleSearchActivity", "moduleList.size(): " + moduleList.size());
				
	    	// Show search results
			ModuleListAdapter adapter = new ModuleListAdapter(this, list);
	    	ListView moduleListView = (ListView) findViewById(R.id.list_search_results);
	    	moduleListView.setAdapter(adapter);
		}
		
		// Turn off the background progress meter in case it's set
		setProgressBarIndeterminateVisibility(false);
    	
    	// Dismiss the progress dialog
    	if (progressDialog != null) {
    		progressDialog.dismiss();
    		progressDialog = null;
    	}
    }
	
	public void onMoreItemsRequested(ModuleList list) {
		
		// Turn on the background activity progress bar too
		setProgressBarIndeterminateVisibility(true);
		
		// Start the search task
		ModuleSearch search = new ModuleSearch(ModuleSearchActivity.this, moduleList, lastSearchText);
		search.setFrom(moduleList.size());
		search.execute();
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
}