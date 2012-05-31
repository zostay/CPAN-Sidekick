/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.qubling.sidekick.module.ModuleSearchActivity;

/**
 * An activity for starting up the application. This is mostly used to give the
 * application a name of "CPAN", but jump into the module search activity. This
 * may do some other initial application setup or switch activities based upon
 * the type of device, etc.
 *
 * @author sterling
 *
 */
public class SidekickLauncher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup BugSense
        Util.setupBugSense(this);

        Intent intent = new Intent(this, ModuleSearchActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }

}
