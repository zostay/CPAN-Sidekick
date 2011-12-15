package com.qubling.sidekick;

import com.qubling.sidekick.metacpan.AuthorByDistributionSearch;
import com.qubling.sidekick.metacpan.FavoriteByDistributionSearch;
import com.qubling.sidekick.metacpan.HttpClientManager;
import com.qubling.sidekick.metacpan.ModulePODFetcher;
import com.qubling.sidekick.metacpan.RatingByDistributionSearch;
import com.qubling.sidekick.metacpan.result.Module;
import com.qubling.sidekick.widget.ModuleHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ModuleViewActivity extends Activity {
	public static final String EXTRA_MODULE = "com.qubling.sidekick.intent.extra.MODULE";
	
	private Module module;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.module_view);
		
		Intent intent = getIntent();
		module = (Module) intent.getParcelableExtra(EXTRA_MODULE);
		
		View moduleHeader = findViewById(R.id.module_view_header);
		ModuleHelper.updateItem(moduleHeader, module);
		
		setTitle(module.getName());
		
		WebView podView = (WebView) findViewById(R.id.module_pod);
		
		podView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView podView, String url) {
				Log.d("ModuleViewActivity", "URL: " + url);
				
				if (url.startsWith("file:///")) {
					return false;
				}
				
				Toast.makeText(ModuleViewActivity.this, "Not Yet Implemented", Toast.LENGTH_SHORT).show();
				
				return true;
			}
		});
		
		int taskCount = 1;
		if (module.getAuthor().isGravatarBitmapNeeded()) taskCount++;
		if (module.getDistribution().isFavoriteNeeded()) taskCount++;
		if (module.getDistribution().isRatingNeeded()) taskCount++;
		
		HttpClientManager clientManager = new HttpClientManager(taskCount);
		new ModulePODFetcher(clientManager, podView).execute(module);
		
		if (module.getAuthor().isGravatarBitmapNeeded())
			new AuthorByDistributionSearch(clientManager, this, module.getAuthor());
		
		if (module.getDistribution().isFavoriteNeeded())
			new FavoriteByDistributionSearch(clientManager, this, module.getDistribution());
		
		if (module.getDistribution().isRatingNeeded())
			new RatingByDistributionSearch(clientManager, this, module.getDistribution());
	}
}
