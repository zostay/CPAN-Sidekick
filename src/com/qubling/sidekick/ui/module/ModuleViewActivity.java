/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.ui.module;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import com.qubling.sidekick.R;
import com.qubling.sidekick.Util;
import com.qubling.sidekick.instance.Module;

/**
 * An activity for viewing a single CPAN module.
 *
 * @author sterling
 *
 */
public class ModuleViewActivity extends ModuleActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.module_view);

        // Setup BugSense
        Util.setupBugSense(this);

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
