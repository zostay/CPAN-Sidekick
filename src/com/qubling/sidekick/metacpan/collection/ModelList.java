package com.qubling.sidekick.metacpan.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.qubling.sidekick.metacpan.result.Model;

public abstract class ModelList<SomeModel extends Model> extends AbstractList<SomeModel> {
	
	public interface OnModelListUpdated<SomeModel extends Model> {
		public void onModelListUpdated(ModelList<SomeModel> modelList);
	}
	
	private ModelList<? extends Model> parent;
	
	private List<SomeModel> modelList;
	private Set<SomeModel> modelSet;

	private List<OnModelListUpdated<SomeModel>> modelListUpdaters = new ArrayList<OnModelListUpdated<SomeModel>>();

	public ModelList() {
		modelSet  = new HashSet<SomeModel>();
		modelList = new ArrayList<SomeModel>();
	}

	public ModelList(Collection<? extends SomeModel> collection) {
		modelSet  = new HashSet<SomeModel>(collection);
		modelList = new ArrayList<SomeModel>(modelSet.size());
		
		addAll(collection);
	}

	public ModelList(int capacity) {
		modelSet  = new HashSet<SomeModel>(capacity);
		modelList = new ArrayList<SomeModel>(capacity);
	}

	public ModelList<? extends Model> getParent() {
		return parent;
	}

	public synchronized void setParent(ModelList<? extends Model> parent) {
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
	
	@Override
	public synchronized void add(int location, SomeModel object) {
		if (modelSet.contains(object)) return;
		
		modelList.add(location, object);
		modelSet.add(object);
	}

	@Override
	public synchronized boolean add(SomeModel object) {
		if (modelSet.contains(object)) return false;
		
		modelList.add(object);
		modelSet.add(object);
		
		return true;
	}

	@Override
	public synchronized boolean addAll(int location, Collection<? extends SomeModel> collection) {
		LinkedHashSet<SomeModel> uniqueModels = new LinkedHashSet<SomeModel>();
		int originalSize = modelSet.size();
		
		for (SomeModel item : collection) {
			if (modelSet.contains(item)) continue;
			uniqueModels.add(item);
		}
		
		modelList.addAll(location, uniqueModels);
		modelSet.addAll(uniqueModels);
		
		return modelSet.size() > originalSize;
	}
	
	@Override
	public synchronized boolean addAll(Collection<? extends SomeModel> collection) {
		int originalSize = modelSet.size();
		
		for (SomeModel item : collection) {
			if (modelSet.contains(item)) continue;
			
			modelList.add(item);
			modelSet.add(item);
		}
		
		return modelSet.size() > originalSize;
	}

	@Override
	public synchronized void clear() {
		modelList.clear();
		modelSet.clear();
	}

	@Override
	public synchronized boolean equals(Object object) {
		if (!(object instanceof List)) return false;
		
		List<?> otherList = (List<?>) object;
		if (otherList.size() != modelList.size()) return false;
		
		for (int i = 0; i < modelList.size(); i++) {
			Object otherItem = otherList.get(i);
			Object myItem    = modelList.get(i);
			
			if (!otherItem.equals(myItem)) return false;
		}
		
		return true;
	}

	@Override
	public synchronized SomeModel get(int location) {
		return modelList.get(location);
	}

	@Override
	public synchronized int hashCode() {
		return modelList.hashCode();
	}

	@Override
	public synchronized int indexOf(Object object) {
		return modelList.indexOf(object);
	}

	@Override
	public synchronized Iterator<SomeModel> iterator() {
		return modelList.iterator();
	}

	@Override
	public synchronized int lastIndexOf(Object object) {
		return modelList.lastIndexOf(object);
	}

	@Override
	public synchronized SomeModel remove(int location) {
		SomeModel item = modelList.remove(location);
		modelSet.remove(item);
		return item;
	}

	@Override
	public synchronized SomeModel set(int location, SomeModel object) {
		SomeModel oldItem = modelList.set(location, object);
		modelSet.remove(oldItem);
		
		// Make sure that if this is a moving item, to rip out the original
		for (int i = 0; i < modelList.size(); i++) {
			if (i == location) continue;
			if (modelList.get(i).equals(object)) {
				modelList.remove(i);
				break;
			}
		}

		return oldItem;
	}

	@Override
	public synchronized boolean contains(Object object) {
		return modelSet.contains(object);
	}

	@Override
	public synchronized boolean isEmpty() {
		return modelList.isEmpty();
	}

	@Override
	public synchronized boolean remove(Object object) {
		modelSet.remove(object);
		return modelList.remove(object);
	}

	@Override
	public synchronized int size() {
		return modelList.size();
	}

	@Override
	public synchronized Object[] toArray() {
		return modelList.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(T[] contents) {
		return modelList.toArray(contents);
	}

}
