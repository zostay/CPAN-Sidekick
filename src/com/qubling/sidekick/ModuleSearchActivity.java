package com.qubling.sidekick;

import com.qubling.sidekick.metacpan.ModuleSearch;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class ModuleSearchActivity extends Activity {
	private static final int DIALOG_SEARCH_PROGRESS = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_search);
        
        final EditText queryText = (EditText) findViewById(R.id.text_search);
        final ListView resultsView = (ListView) findViewById(R.id.list_search_results);
        
        final ImageButton searchButton = (ImageButton) findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View searchButton) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(searchButton.getWindowToken(), 0);
				Dialog dialog = ProgressDialog.show(ModuleSearchActivity.this, "", "Searching CPAN...", true);
				new ModuleSearch(resultsView, dialog, queryText.getText().toString()).execute();
			}
		});
        
        queryText.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_NUMPAD_ENTER:
					searchButton.performClick();
					return true;
				}
				return false;
			}
		});
    }
}