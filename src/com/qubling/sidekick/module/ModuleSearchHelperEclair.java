package com.qubling.sidekick.module;

import com.qubling.sidekick.R;
import com.qubling.sidekick.R.id;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

public class ModuleSearchHelperEclair extends ModuleSearchHelper {
    private boolean searchRunning = false;

	public ModuleSearchHelperEclair(ModuleSearchActivity activity) {
		super(activity);
	}

	@Override
	public void onCreate(Bundle state) {

        final EditText queryText = (EditText) getActivity().findViewById(R.id.text_search);

        final ImageButton searchButton = (ImageButton) getActivity().findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View searchButton) {

                    // Hide the screen keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchButton.getWindowToken(), 0);

                    getActivity().doNewSearch(queryText.getText().toString());
            }
        });

        queryText.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // Support KEYCODE_NUMPAD_ENTER added in API 11
                int KEYCODE_NUMPAD_ENTER = KeyEvent.KEYCODE_UNKNOWN;
                try {
                    KEYCODE_NUMPAD_ENTER = KeyEvent.class.getField("KEYCODE_NUMPAD_ENTER").getInt(null);
                }
                catch (Throwable t) {
                    // ignore
                }

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KEYCODE_NUMPAD_ENTER) {
                    if (!searchRunning)
                        searchButton.performClick();
                    return true;
                }

                return false;
            }
        });
	}

	@Override
	public Boolean onSearchRequested() {
		EditText queryText = (EditText) getActivity().findViewById(R.id.text_search);
		queryText.requestFocus();
		return true;
	}
}
