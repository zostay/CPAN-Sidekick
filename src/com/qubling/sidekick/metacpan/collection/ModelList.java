package com.qubling.sidekick.metacpan.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.qubling.sidekick.metacpan.result.Model;

public abstract class ModelList<SomeModel extends Model> extends ArrayList<SomeModel> {
	private static final long serialVersionUID = 8391018065517479242L;
	
	public interface OnModelListUpdated<SomeModel extends Model> {
		public void onModelListUpdated(ModelList<SomeModel> modelList);
	}
	
	private ModelList<? extends Model> parent;

	private List<OnModelListUpdated<SomeModel>> modelListUpdaters = new ArrayList<OnModelListUpdated<SomeModel>>();

	public ModelList() {
		super();
	}

	public ModelList(Collection<? extends SomeModel> collection) {
		super(collection);
	}

	public ModelList(int capacity) {
		super(capacity);
	}

	public ModelList<? extends Model> getParent() {
		return parent;
	}

	public void setParent(ModelList<? extends Model> parent) {
		this.parent = parent;
	}
	
	public synchronized void addModelListUpdatedListener(OnModelListUpdated<SomeModel> listener) {
		modelListUpdaters.add(listener);
	}
	
	public synchronized void removeModelListUpdatedListener(OnModelListUpdated<SomeModel> listener) {
		modelListUpdaters.remove(listener);
	}
	
	public synchronized void notifyModelListUpdated() {
		for (OnModelListUpdated<SomeModel> listener : modelListUpdaters) {
			listener.onModelListUpdated(this);
		}
		
		// Cascade the notifications up to the parent, if any
		if (parent != null)
			parent.notifyModelListUpdated();
	}
}
