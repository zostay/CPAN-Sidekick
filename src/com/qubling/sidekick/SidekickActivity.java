package com.qubling.sidekick;

import com.qubling.sidekick.metacpan.ModuleSearch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class SidekickActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final EditText queryText = (EditText) findViewById(R.id.text_search);
        final ListView resultsView = (ListView) findViewById(R.id.list_search_results);
        
        ImageButton searchButton = (ImageButton) findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View searchButton) {
				new ModuleSearch(resultsView, queryText.getText().toString()).execute();
			}
		});
    }
}