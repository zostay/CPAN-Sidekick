package com.qubling.sidekick;

import com.qubling.sidekick.metacpan.result.Module;
import com.qubling.sidekick.widget.ModuleHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
	}
}
