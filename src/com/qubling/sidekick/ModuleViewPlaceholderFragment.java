package com.qubling.sidekick;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ModuleViewPlaceholderFragment extends Fragment implements ModuleViewThingyFragment {
    
    @Override
    public boolean isPlaceholder() {
    	return false;
    }
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflater.inflate(R.layout.module_view_placeholder_fragment, container, false);
	}
}
