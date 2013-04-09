/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.ui.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.qubling.sidekick.R;
import com.qubling.sidekick.Util;
import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.ui.AboutDialogFragment;
import com.qubling.sidekick.util.AuthenticationHelper;
import com.qubling.sidekick.util.ConnectivityCheck;
import com.qubling.sidekick.widget.ModuleListAdapter;

/**
 * An activity for searching for CPAN modules.
 *
 * @author sterling
 *
 */
public class ModuleSearchActivity extends ModuleActivity implements SearchableActivity {
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
        
//        AccountManager am = AccountManager.get(this);
//        Account[] accounts = am.getAccounts();
//        for (Account account : accounts) {
//            Log.d("ModuleSearchActivity", "ACCOUNT " + account.name + ", " + account.type);
//        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//        case R.id.menu_sign_in_out:
//            AuthenticationHelper auth = new AuthenticationHelper(this);
//            Map<String, AuthenticatorDescription> authenticatorMap = auth.getAuthenticatorMap();
//            List<Account> accounts = auth.getAccounts();
//            
//            if (accounts.size() == 0) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                
//                builder.setTitle(R.string.error_no_accounts_to_pick_title);
//                builder.setIcon(android.R.drawable.ic_dialog_alert);
//                builder.setMessage(R.string.error_no_accounts_to_pick);
//                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                builder.show();
//            }
//            else {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                
//                List<Map<String, Object>> accountList = new ArrayList<Map<String,Object>>(accounts.size());
//                for (Account account : accounts) {
//                    Map<String, Object> accountMap = new HashMap<String, Object>(2);
//                    AuthenticatorDescription authDesc = authenticatorMap.get(account.type);
//                    String authName = account.type;
//                    Resources authRes = null;
//                    try {
//                        authRes = getPackageManager().getResourcesForApplication(authDesc.packageName);
//                        authName = authRes.getString(authDesc.labelId);
//                    }
//                    catch (NameNotFoundException e) {
//                        Log.e("ModuleSearchActivity", "Could not find label for " + account.type);
//                    }
//                    
//                    accountMap.put("label", account.name + " (" + authName + ")");
//                    if (authRes != null) {
//                        accountMap.put("icon", new Pair<Resources, Integer>(authRes, authDesc.iconId));
//                    }
//                    
//                    accountList.add(accountMap);
//                }
//                
//                String[] from = new String[] { "label", "icon" };
//                int[] to = new int[] { R.id.account_name, R.id.account_type_icon };
//                
//                SimpleAdapter accountAdapter = new SimpleAdapter(this, accountList, R.layout.account_list_item, from, to);
//                accountAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
//                    
//                    @Override
//                    public boolean setViewValue(View view, Object data, String textRepresentation) {
//                        if (view instanceof ImageView && data instanceof Pair<?, ?>) {
//                            ImageView image = (ImageView) view;
//                            Pair<?, ?> pair = (Pair<?, ?>) data;
//                            
//                            try {
//                                Resources accountResources = (Resources) pair.first;
//                                Integer iconId = (Integer) pair.second;
//                                Drawable accountIcon = accountResources.getDrawable(iconId);
//                                image.setImageDrawable(accountIcon);
//                            }
//                            
//                            // This will NEVER happen! ... yeah right ...
//                            catch (ClassCastException e) {
//                                return false;
//                            }
//                            
//                            return true;
//                        }
//                        
//                        return false;
//                    }
//                });
//                
//                builder.setTitle(R.string.dialog_select_account);
//                builder.setAdapter(accountAdapter, null);
//                builder.show();
//            }
//            return true;
        case R.id.menu_about:
            DialogFragment dialog = new AboutDialogFragment();
            dialog.show(getSupportFragmentManager(), "about");
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
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
    public void onModuleClick(Module currentModule) {
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
	        moduleViewIntent.putExtra(ModuleActivity.ExtraModule(), currentModule);
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
