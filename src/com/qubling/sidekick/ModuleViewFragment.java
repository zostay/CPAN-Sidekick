package com.qubling.sidekick;

import java.util.Stack;

import com.qubling.sidekick.api.HttpClientManager;
import com.qubling.sidekick.api.cpan.MetaCPANAPI;
import com.qubling.sidekick.api.cpan.ModulePODFetcher;
import com.qubling.sidekick.cpan.collection.ModelList;
import com.qubling.sidekick.cpan.collection.ModuleList;
import com.qubling.sidekick.cpan.result.Module;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ModuleViewFragment extends ModuleFragment {
    private Stack<Module> moduleHistory = new Stack<Module>();
    private Module module;
    
    public void setModule(Module module) {
    	
    	// If we have a module in place, push it on to the history stack
    	if (this.module != null) {
    		moduleHistory.push(module);
    	}
    	
    	this.module = module;
    	
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        ModuleInfoFragment fragment = (ModuleInfoFragment) fragmentManager.findFragmentById(R.id.module_info_fragment);
        fragment.setModule(module);
    }
    
    public Module getModule() {
    	return module;
    }

    @Override
    public void onActivityCreated(Bundle state) {
    	super.onActivityCreated(state);

        WebView podView = (WebView) getActivity().findViewById(R.id.module_pod);

        podView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView podView, String url) {
//                Log.d("ModuleViewActivity", "URL: " + url);

                // Let the built-in handler get all the internal URLs
                if (url.startsWith("file:///android_asset/web/")) {
                    return false;
                }
                
                // Let the built-in handler get all the POD API URLs
                else if (url.startsWith(MetaCPANAPI.METACPAN_API_POD_URL)) {
                	return false;
                }

                // Rewrite MetaCPAN module URLs to fetch the POD
                else if (url.startsWith(MetaCPANAPI.METACPAN_MODULE_URL)) {
                    moduleHistory.push(module);

                    String moduleName = url.substring(MetaCPANAPI.METACPAN_MODULE_URL.length());
                    module = new Module(moduleName);

                    fetchModule();

                    return true;
                }

                // For anything else, load the browser
                else {
                	Intent intent = new Intent();
                	intent.setAction(Intent.ACTION_VIEW);
                	intent.setData(Uri.parse(url));
                	startActivity(intent);
                	
                	return true;
                }
            }
        });

        fetchModule();
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflater.inflate(R.layout.module_view_fragment, container, false);
	}
    
    public void fetchModule() {
    	
    	// No module loaded, skip it
    	if (module == null) return;
    	
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        final ModuleInfoFragment fragment = (ModuleInfoFragment) fragmentManager.findFragmentById(R.id.module_info_fragment);
        
        fetchModule(module,
                new ModuleList.OnModuleListUpdated() {
                    @Override
                    public void onModelListUpdated(ModelList<Module> modelList) {
//                        Log.d("ModuleViewActivity", "onModelListUpdated updating header");
                        fragment.updateView();
                    }
                },
                new ModuleFetchTask() {
                    @Override
                    public void doFetchTask(HttpClientManager clientManager, Module module) {
                        WebView podView = (WebView) getActivity().findViewById(R.id.module_pod);
                        new ModulePODFetcher(clientManager, podView).execute(module);
                    }
                });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && !moduleHistory.empty()) {

            module = moduleHistory.pop();
        	
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            ModuleInfoFragment fragment = (ModuleInfoFragment) fragmentManager.findFragmentById(R.id.module_info_fragment);
            fragment.setModule(module);

            fetchModule();

            return true;
        }

        return false;
    }

}
