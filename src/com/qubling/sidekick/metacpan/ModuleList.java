package com.qubling.sidekick.metacpan;

import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;

import com.qubling.sidekick.R;
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
	
	private synchronized void notifyModuleListUpdater() {
		if (moduleListUpdater != null)
			moduleListUpdater.onModuleListUpdate(this);
	}

	public ModuleSearchAdapter toModuleSearchAdapter(Context context) {
		return new ModuleSearchAdapter(context, R.layout.module_search_list_item, this);
	}

	@Override
	public void add(int index, Module object) {
		super.add(index, object);
		notifyModuleListUpdater();
	}

	@Override
	public boolean add(Module object) {
		boolean result = super.add(object);
		notifyModuleListUpdater();
		return result;
	}

	@Override
	public boolean addAll(Collection<? extends Module> collection) {
		boolean result = super.addAll(collection);
		notifyModuleListUpdater();
		return result;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Module> collection) {
		boolean result = super.addAll(index, collection);
		notifyModuleListUpdater();
		return result;
	}

	@Override
	public void clear() {
		super.clear();
		notifyModuleListUpdater();
	}

	@Override
	public Module remove(int index) {
		Module result = super.remove(index);
		notifyModuleListUpdater();
		return result;
	}

	@Override
	public boolean remove(Object object) {
		boolean result = super.remove(object);
		notifyModuleListUpdater();
		return result;
	}

	@Override
	public Module set(int index, Module object) {
		Module result = super.set(index, object);
		notifyModuleListUpdater();
		return result;
	}
}
