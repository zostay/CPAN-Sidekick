/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick;

import java.util.Stack;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.qubling.sidekick.metacpan.HttpClientManager;
import com.qubling.sidekick.metacpan.MetaCPANAPI;
import com.qubling.sidekick.metacpan.ModulePODFetcher;
import com.qubling.sidekick.metacpan.collection.ModelList;
import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;
import com.qubling.sidekick.widget.ModuleHelper;

/**
 * An activity for viewing a single CPAN module.
 * 
 * @author sterling
 *
 */
public class ModuleViewActivity extends ModuleActivity {
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

        fetchModule(module,
                new ModuleList.OnModuleListUpdated() {
                    @Override
                    public void onModelListUpdated(ModelList<Module> modelList) {
//                        Log.d("ModuleViewActivity", "onModelListUpdated updating header");
                        ModuleHelper.updateItem(moduleHeader, module);
                    }
                },
                new ModuleFetchTask() {
                    @Override
                    public void doFetchTask(HttpClientManager clientManager, Module module) {
                        WebView podView = (WebView) findViewById(R.id.module_pod);
                        new ModulePODFetcher(clientManager, podView).execute(module);
                    }
                });
    }
}
