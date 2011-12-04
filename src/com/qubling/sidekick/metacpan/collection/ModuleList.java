package com.qubling.sidekick.metacpan.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.qubling.sidekick.metacpan.result.Module;

public class ModuleList extends ArrayList<Module> {
	
	public interface OnModuleListUpdated {
		public void onModuleListUpdate(ModuleList moduleList);
	}
	
	public interface OnMoreItemsRequested {
		public void onMoreItemsRequested(ModuleList moduleList);
	}

	private static final long serialVersionUID = 2182482391570721434L;
	
	private int totalCount;

	private List<OnModuleListUpdated> moduleListUpdaters;
	private List<OnMoreItemsRequested> moreItemsRequested;
	
	public ModuleList() {
		this(0);
	}

	public ModuleList(int totalCount) {
		super();
		
		this.totalCount = totalCount;
		this.moduleListUpdaters = new ArrayList<OnModuleListUpdated>();
		this.moreItemsRequested = new ArrayList<OnMoreItemsRequested>();
	}
	
	public ModuleList(Module[] modules, int totalCount) {
		super(modules.length);
		
		this.totalCount = totalCount;
		this.moduleListUpdaters = new ArrayList<OnModuleListUpdated>();
		this.moreItemsRequested = new ArrayList<OnMoreItemsRequested>();
		
		Collections.addAll(this, modules);
	}

	public ModuleList(Collection<? extends Module> collection, int totalCount) {
		super(collection);
		
		this.totalCount = totalCount;
		this.moduleListUpdaters = new ArrayList<OnModuleListUpdated>();
		this.moreItemsRequested = new ArrayList<OnMoreItemsRequested>();
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	
	public synchronized void addModuleListUpdater(OnModuleListUpdated listener) {
		moduleListUpdaters.add(listener);
	}
	
	public synchronized void removeModuleListUpdater(OnModuleListUpdated listener) {
		moduleListUpdaters.remove(listener);
	}
	
	public synchronized void notifyModuleListUpdaters() {
		for (OnModuleListUpdated listener : moduleListUpdaters) {
			listener.onModuleListUpdate(this);
		}
	}
	
	public synchronized void addMoreItemsRequestedListener(OnMoreItemsRequested listener) {
		moreItemsRequested.add(listener);
	}
	
	public synchronized void removeMoreItemsReqeustedListener(OnMoreItemsRequested listener) {
		moreItemsRequested.remove(listener);
	}
	
	public synchronized void requestMoreItems() {
		for (OnMoreItemsRequested listener : moreItemsRequested) {
			listener.onMoreItemsRequested(this);
		}
	}
}
