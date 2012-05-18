package com.qubling.sidekick.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ResultsNeedingUpdate<SomeInstance extends Instance<SomeInstance>> 
	implements ResultSet<SomeInstance> {
	
	private UpdateFetcher<SomeInstance> filter;
	private ArrayList<SomeInstance> filteredIndex;
	private Results<SomeInstance> unfilteredResultSet;
	
	public ResultsNeedingUpdate(UpdateFetcher<SomeInstance> filter, Results<SomeInstance> results) {
		this.filter = filter;
		this.filteredIndex = new ArrayList<SomeInstance>();
		this.unfilteredResultSet = results;
	}
	
	public UpdateFetcher<SomeInstance> getFilter() {
		return filter;
	}
	
	public ResultSet<SomeInstance> getUnfilteredResultSet() {
		return unfilteredResultSet;
	}
	
	@Override
	public boolean add(SomeInstance instance) {
		boolean modified = unfilteredResultSet.add(instance);
		
		if (!modified) return false;
		
		if (filter.needsUpdate(instance)) {
			filteredIndex.add(instance);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean addAll(Collection<? extends SomeInstance> instances) {
		boolean modified = unfilteredResultSet.addAll(instances);
		
		if (!modified) return false;
		
		modified = false;
		for (SomeInstance instance : instances) {
			if (filter.needsUpdate(instance)) {
				filteredIndex.add(instance);
				modified = true;
			}
		}
		
		return modified;
	}
	
	@Override
	public void clear() {
		unfilteredResultSet.clear();
		filteredIndex.clear();
	}
	
	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean modified = unfilteredResultSet.retainAll(collection);
		
		if (!modified) return false;
		
		modified = false;
		ArrayList<SomeInstance> keepers = new ArrayList<SomeInstance>(filteredIndex.size());
		for (SomeInstance instance : filteredIndex) {
			if (collection.contains(instance)) {
				keepers.add(instance);
			}
			else {
				modified = true;
			}
		}
		
		if (modified) {
			filteredIndex = keepers;
		}
		
		return modified;
	}
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean modified = unfilteredResultSet.removeAll(collection);
		
		if (!modified) return false;
		
		modified = false;
		for (Object o : collection) {
			if (filteredIndex.remove(o)) {
				modified = true;
			}
		}
		
		return modified;
	}
	
	@Override
	public boolean remove(Object o) {
		unfilteredResultSet.remove(o);
		return filteredIndex.remove(o);
	}
	
	public SomeInstance get(String key) {
		SomeInstance instance = unfilteredResultSet.get(key);
		if (instance != null && filteredIndex.contains(instance)) {
			return instance;
		}
		return null;
	}
	
	public SomeInstance get(int index) {
		return filteredIndex.get(index);
	}
	
	@Override
	public boolean contains(Object o) {
		return filteredIndex.contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> collection) {
		return filteredIndex.containsAll(collection);
	}
	
	@Override
	public int size() {
		return filteredIndex.size();
	}
	
	@Override
	public int getTotalSize() {
		return unfilteredResultSet.getTotalSize();
    }

	@Override
	public boolean isEmpty() {
		return filteredIndex.isEmpty();
	}
	
	@Override
	public Object[] toArray() {
		return filteredIndex.toArray();
	}
	
	@Override
	public <OtherInstance> OtherInstance[] toArray(OtherInstance[] array) {
		return filteredIndex.toArray(array);
	}

	@Override
    public Iterator<SomeInstance> iterator() {
		return filteredIndex.iterator();
    }
	
	public ArrayList<SomeInstance> toArrayList() {
		ArrayList<SomeInstance> arrayList = new ArrayList<SomeInstance>(filteredIndex.size());
		arrayList.addAll(filteredIndex);
		return arrayList;
	}

	@Override
    public <OtherInstance extends Instance<OtherInstance>> void addRemap(ResultSet<OtherInstance> others, ResultSet.Remap<OtherInstance, SomeInstance> map) {
	    unfilteredResultSet.addRemap(others, map);
	    
	    for (OtherInstance otherInstance : others) {
		    for (SomeInstance instance : map.map(otherInstance)) {
		    	if (filter.needsUpdate(instance)) {
		    		filteredIndex.add(instance);
		    	}
		    }
	    }
    }
}
