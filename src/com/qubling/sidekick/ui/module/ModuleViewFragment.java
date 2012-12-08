package com.qubling.sidekick.ui.module;

import java.util.Collections;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.qubling.sidekick.R;
import com.qubling.sidekick.fetch.Fetcher;
import com.qubling.sidekick.fetch.UpdateFetcher;
import com.qubling.sidekick.fetch.cpan.CPANFetcher;
import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.model.ModuleModel;
import com.qubling.sidekick.search.ResultSet;
import com.qubling.sidekick.search.Schema;
import com.qubling.sidekick.search.Search;
import com.qubling.sidekick.util.LinkedListStack;
import com.qubling.sidekick.util.Stack;
import com.qubling.sidekick.widget.ModuleHelper;

public class ModuleViewFragment extends ModuleFragment implements ModuleViewThingyFragment, Fetcher.OnFinished<Module> {
    private Stack<Module> moduleHistory = new LinkedListStack<Module>();
	private Schema searchSession;
    private Module module;

    public void setModule(Module module) {
//    	Log.d("ModuleViewFragment", "setModule(): " + new Gson().toJson(module));

    	// If we have a module in place, push it on to the history stack
    	if (this.module != null) {
    		moduleHistory.push(module);
    	}

    	this.module = module;
    	this.module.attachToModel(searchSession.getModuleModel());

    	View moduleInfo = getActivity().findViewById(R.id.module_info);
    	ModuleHelper.updateItem(moduleInfo, module);
    }

    public Module getModule() {
    	return module;
    }
    
    @Override
    public void onCreate(Bundle state) {
    	super.onCreate(state);
    	
	    searchSession = new Schema(this.getActivity());
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
                else if (url.startsWith(CPANFetcher.METACPAN_API_POD_URL)) {
                	return false;
                }

                // Rewrite MetaCPAN module URLs to fetch the POD
                else if (url.startsWith(CPANFetcher.METACPAN_MODULE_URL)) {
                    
                    moduleHistory.push(module);

                    String moduleName = url.substring(CPANFetcher.METACPAN_MODULE_URL.length());
                    module = searchSession.getModuleModel().acquireInstance(moduleName);

                    fetchModule();

                    return true;
                }
                
                // Rewrite MetaCPAN secure module URLs to fetch the POD
                else if (url.startsWith(CPANFetcher.METACPAN_SECURE_MODULE_URL)) {
                    
                    moduleHistory.push(module);

                    String moduleName = url.substring(CPANFetcher.METACPAN_SECURE_MODULE_URL.length());
                    module = searchSession.getModuleModel().acquireInstance(moduleName);

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

        if (state != null && state.containsKey("viewModule")) {
        	module = (Module) state.getParcelable("viewModule");
        	module.attachToModel(searchSession.getModuleModel());
        }

        if (state != null && state.containsKey("viewModuleHistory")) {
        	Parcelable[] historyArray = state.getParcelableArray("viewModuleHistory");
        	Module[] moduleHistoryArray = new Module[historyArray.length];
        	System.arraycopy(historyArray, 0, moduleHistoryArray, 0, historyArray.length);
        	moduleHistory = new LinkedListStack<Module>();
        	Collections.addAll(moduleHistory, moduleHistoryArray);
        }

        fetchModule();
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflater.inflate(R.layout.module_view_fragment, container, false);
	}

    @Override
    public void onSaveInstanceState(Bundle state) {
	    super.onSaveInstanceState(state);

	    Parcelable[] historyArray = new Parcelable[moduleHistory.size()];
	    moduleHistory.toArray(historyArray);
	    state.putParcelableArray("viewModuleHistory", historyArray);

	    state.putParcelable("viewModule", module);
    }
    
    public Search<Module> buildSearch() {
    	ModuleModel modules = searchSession.getModuleModel();
    	Fetcher<Module> moduleFetch = modules.fetch();
    	moduleFetch.getResultSet().add(module);
    	UpdateFetcher<Module> fetchPod = modules.fetchPod();
    	
    	// TODO These are common with ModuleSearchFragment, SHARE!!!
        UpdateFetcher<Module> fetchFavorites = modules.fetchReleaseFavorites("");
        UpdateFetcher<Module> fetchRatings   = modules.fetchReleaseRatings();
        UpdateFetcher<Module> fetchAuthors   = modules.fetchAuthors();
        UpdateFetcher<Module> fetchGravatars = modules.fetchGravatars(GRAVATAR_DP_SIZE);
    	
        @SuppressWarnings("unchecked")
        Search<Module> search = searchSession.doFetch(moduleFetch, this)
        		.thenDoFetch(
        				fetchPod,
        				fetchFavorites, 
        				fetchRatings,
        				fetchAuthors
        						.thenDoFetch(fetchGravatars)
        			);
        
        search.addOnSearchActivityListener(this.getModuleActivity());
        
        return search;
    }

	public void fetchModule() {

    	// No module loaded, skip it
    	if (module == null) return;
    	
    	Search<Module> search = buildSearch();
    	search.start();
    }
	
	public void onFinishedFetch(Fetcher<Module> fetcher, ResultSet<Module> modules) {
	    
	    // Don't do anything if we don't have an activity (i.e., don't NPE either)
	    if (getActivity() == null) return;

    	View moduleInfo = getActivity().findViewById(R.id.module_info);
    	
		ModuleHelper.updateItem(moduleInfo, module);
		
		if (module.getRawPod() != null) {
			WebView podView = (WebView) getActivity().findViewById(R.id.module_pod);
			
			// Avoid an NPE here, just in case
			if (podView == null) return;
			
			String formattedPod = "<html><head><link href=\"style/pod.css\" type=\"text/css\" rel=\"stylesheet\"/></head><body class=\"pod\">"
                    + module.getRawPod()
                    + "</body></html>";

			podView.loadDataWithBaseURL("file:///android_asset/web/pod/", formattedPod, "text/html", "UTF-8", null);
		}
	}

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && !moduleHistory.isEmpty()) {

            module = moduleHistory.pop();

        	View moduleInfo = getActivity().findViewById(R.id.module_info);
        	ModuleHelper.updateItem(moduleInfo, module);

            fetchModule();

            return true;
        }

        return false;
    }

}
