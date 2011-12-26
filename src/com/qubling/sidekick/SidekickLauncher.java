/*
 * Copyright 2011 Qubling Software, LLC.
 * 
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SidekickLauncher extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent(this, ModuleSearchActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		finish();
	}

}
