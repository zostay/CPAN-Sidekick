/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.module;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import com.bugsense.trace.BugSenseHandler;
import com.qubling.sidekick.R;
import com.qubling.sidekick.Util;
import com.qubling.sidekick.R.id;
import com.qubling.sidekick.R.layout;
import com.qubling.sidekick.cpan.result.Module;

/**
 * An activity for viewing a single CPAN module.
 *
 * @author sterling
 *
 */
public class ModuleViewActivity extends ModuleActivity {
    public static final String EXTRA_MODULE = "com.qubling.sidekick.intent.extra.MODULE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.module_view);

        // Setup BugSense
        BugSenseHandler.setup(this, Util.BUGSENSE_API_KEY);

        Intent intent = getIntent();
        Module 	module = (Module) intent.getParcelableExtra(EXTRA_MODULE);

        setTitle(module.getName());

        FragmentManager fragmentManager = getSupportFragmentManager();
        ModuleViewFragment fragment = (ModuleViewFragment) fragmentManager.findFragmentById(R.id.module_view_fragment);
        fragment.setModule(module);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	FragmentManager fragmentManager = getSupportFragmentManager();
    	ModuleViewFragment fragment = (ModuleViewFragment) fragmentManager.findFragmentById(R.id.module_view_fragment);

    	boolean result = fragment.onKeyDown(keyCode, event);
    	if (result) {
    		return result;
    	}
    	else {
    		return super.onKeyDown(keyCode, event);
    	}
    }
}
