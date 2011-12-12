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
