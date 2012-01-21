package com.qubling.sidekick;

import com.qubling.sidekick.cpan.result.Module;
import com.qubling.sidekick.widget.ModuleHelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ModuleInfoFragment extends Fragment {

	private Module module;
	
	public Module getModule() {
    	return module;
    }

	public void setModule(Module module) {
    	this.module = module;
    	updateView();
    }
	
	public void updateView() {
		if (module == null) return;
		ModuleHelper.updateItem(getView(), module);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflater.inflate(R.layout.module_list_item, container, false);
	}
}
