package com.qubling.sidekick.ui.module;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qubling.sidekick.R;
import com.qubling.sidekick.widget.ModuleListAdapter;

public class ModuleViewPlaceholderFragment extends Fragment implements ModuleViewThingyFragment {

	private boolean showingExtraBubble = false;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflater.inflate(R.layout.module_view_placeholder_fragment, container, false);
	}

	@Override
    public void onActivityCreated(Bundle state) {
	    super.onActivityCreated(state);

	    if (state == null) return;

	    boolean showingExtraBubble = state.getBoolean("placeholderShowingExtraBubble");
	    if (showingExtraBubble) {
	    	showExtraBubble();
	    }
    }

	@Override
    public void onSaveInstanceState(Bundle state) {
	    super.onSaveInstanceState(state);

	    state.putBoolean("placeholderShowingExtraBubble", showingExtraBubble);
    }

	public void onSearchCompleted(ModuleListAdapter adapter) {
    	if (adapter.getCount() > 0) {
			showExtraBubble();
    	}
	}

	private void showExtraBubble() {
		TextView view = (TextView) getActivity().findViewById(R.id.results_help_bubble);

		// Can happen in certain edge cases... just ignore.
		if (view == null) return;

		view.setVisibility(View.VISIBLE);
		showingExtraBubble = true;
	}
}
