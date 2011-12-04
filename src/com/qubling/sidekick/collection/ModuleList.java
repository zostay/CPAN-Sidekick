package com.qubling.sidekick.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.content.Context;

import com.qubling.sidekick.R;
import com.qubling.sidekick.metacpan.ModuleSearchAdapter;
import com.qubling.sidekick.metacpan.result.Module;

public class ModuleList extends ArrayList<Module> {
	
	public interface OnModuleListUpdated {
		public void onModuleListUpdate(ModuleList moduleList);
	}

	private static final long serialVersionUID = 2182482391570721434L;
	
	private OnModuleListUpdated moduleListUpdater;

	public ModuleList() {
		super();
	}
	
	public ModuleList(Module[] modules) {
		super(modules.length);
		
		Collections.addAll(this, modules);
	}

	public ModuleList(Collection<? extends Module> collection) {
		super(collection);
	}

	public ModuleList(int capacity) {
		super(capacity);
	}

	public synchronized OnModuleListUpdated getModuleListUpdater() {
		return moduleListUpdater;
	}

	public synchronized void setModuleListUpdater(OnModuleListUpdated moduleListUpdater) {
		this.moduleListUpdater = moduleListUpdater;
	}
	
	public synchronized void notifyModuleListUpdater() {
		if (moduleListUpdater != null)
			moduleListUpdater.onModuleListUpdate(this);
	}

	public ModuleSearchAdapter toModuleSearchAdapter(Context context) {
		return new ModuleSearchAdapter(context, R.layout.module_search_list_item, this);
	}
}
