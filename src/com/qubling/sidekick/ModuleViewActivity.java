package com.qubling.sidekick;

import java.util.Stack;

import com.qubling.sidekick.metacpan.AuthorByDistributionSearch;
import com.qubling.sidekick.metacpan.FavoriteByDistributionSearch;
import com.qubling.sidekick.metacpan.HttpClientManager;
import com.qubling.sidekick.metacpan.MetaCPANAPI;
import com.qubling.sidekick.metacpan.ModuleFetcher;
import com.qubling.sidekick.metacpan.ModulePODFetcher;
import com.qubling.sidekick.metacpan.RatingByDistributionSearch;
import com.qubling.sidekick.metacpan.collection.DistributionList;
import com.qubling.sidekick.metacpan.collection.ModelList;
import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;
import com.qubling.sidekick.widget.ModuleHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ModuleViewActivity extends Activity {
	public static final String EXTRA_MODULE = "com.qubling.sidekick.intent.extra.MODULE";
	
	private Stack<Module> moduleHistory = new Stack<Module>();
	private Module module;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.module_view);
		
		Intent intent = getIntent();
		module = (Module) intent.getParcelableExtra(EXTRA_MODULE);
		
		final View moduleHeader = findViewById(R.id.module_view_header);
		ModuleHelper.updateItem(moduleHeader, module);
		
		setTitle(module.getName());
		
		WebView podView = (WebView) findViewById(R.id.module_pod);
		
		podView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView podView, String url) {
				Log.d("ModuleViewActivity", "URL: " + url);
				
				if (url.startsWith("file:///android_asset/web/")) {
					return false;
				}
				
				else if (url.startsWith(MetaCPANAPI.METACPAN_MODULE_URL)) {
					moduleHistory.push(module);
					
					String moduleName = url.substring(MetaCPANAPI.METACPAN_MODULE_URL.length());
					module = new Module(moduleName);
					
					fetchModule();
					
					return true;
				}
				
				Toast.makeText(ModuleViewActivity.this, R.string.not_yet_implemented, Toast.LENGTH_SHORT).show();
				
				return true;
			}
		});
		
		fetchModule();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !moduleHistory.empty()) {
			
			module = moduleHistory.pop();
			
			View moduleHeader = findViewById(R.id.module_view_header);
			ModuleHelper.updateItem(moduleHeader, module);
			
			fetchModule();
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	private void fetchModule() {
		final View moduleHeader = findViewById(R.id.module_view_header);
		
		// In this case we will fetchModule() twice, once to load the basic module info and 
		// then again afterwards to load the POD and other stuff that depends on the first 
		// bit of info
		if (module.isModuleFetchNeeded()) {
			HttpClientManager clientManager = new HttpClientManager(1);
			
			ModuleList moduleList = module.toModuleList();
			
			moduleList.addModelListUpdatedListener(new ModuleList.OnModuleListUpdated() {
				@Override
				public void onModelListUpdated(ModelList<Module> modelList) {
					Log.d("ModuleViewActivity", "onModelListUpdated updating header");
					ModuleHelper.updateItem(moduleHeader, module);
					
					fetchModule();
				}
			});
			
			new ModuleFetcher(clientManager, moduleList).execute();
			return;
		}
		
		WebView podView = (WebView) findViewById(R.id.module_pod);
		
		int taskCount = 1;
		if (module.getAuthor().isGravatarURLNeeded() || module.getAuthor().isGravatarBitmapNeeded()) taskCount++;
		if (module.getDistribution().isFavoriteNeeded()) taskCount++;
		if (module.getDistribution().isRatingNeeded()) taskCount++;
		
		HttpClientManager clientManager = new HttpClientManager(taskCount);
		new ModulePODFetcher(clientManager, podView).execute(module);
		
		if (taskCount > 1) {
			ModuleList moduleList = module.toModuleList();
			
			moduleList.addModelListUpdatedListener(new ModuleList.OnModuleListUpdated() {
				@Override
				public void onModelListUpdated(ModelList<Module> modelList) {
					Log.d("ModuleViewActivity", "onModelListUpdated updating header");
					ModuleHelper.updateItem(moduleHeader, module);
				}
			});
			
			if (module.getAuthor().isGravatarURLNeeded() || module.getAuthor().isGravatarBitmapNeeded()) {
				Log.d("ModuleViewActivity", "isGravatarBitmapNeeded: true");
				new AuthorByDistributionSearch(clientManager, this, moduleList.extractAuthorList()).execute();
			}
			
			if (module.getDistribution().isFavoriteNeeded() || module.getDistribution().isRatingNeeded()) {
				DistributionList distributionList = moduleList.extractDistributionList();
				if (module.getDistribution().isFavoriteNeeded())
					new FavoriteByDistributionSearch(clientManager, this, distributionList).execute();
				
				if (module.getDistribution().isRatingNeeded())
					new RatingByDistributionSearch(clientManager, this, distributionList).execute();
			}
		}
		
	}
}
