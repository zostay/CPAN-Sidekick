package com.qubling.sidekick.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ResultsForUpdate<SomeInstance extends Instance<SomeInstance>> 
	implements ResultSet<SomeInstance>, ResultSet.OnChangeListener<SomeInstance> {
	
	private UpdateFetcher<SomeInstance> filter;
	private ArrayList<SomeInstance> filteredIndex;
	private ResultSet<SomeInstance> unfilteredResultSet;
	
	public ResultsForUpdate(UpdateFetcher<SomeInstance> filter, ResultSet<SomeInstance> results) {
		this.filter = filter;
		this.filteredIndex = new ArrayList<SomeInstance>();
		this.unfilteredResultSet = results;
		
		unfilteredResultSet.addOnChangeListener(this);
	}
	
	public UpdateFetcher<SomeInstance> getFilter() {
		return filter;
	}
	
	public ResultSet<SomeInstance> getUnfilteredResultSet() {
		return unfilteredResultSet;
	}
	
	@Override
	public boolean add(SomeInstance instance) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Do not modify ResultsForUpdate");
	}
	
	@Override
	public boolean addAll(Collection<? extends SomeInstance> instances) {
		throw new UnsupportedOperationException("Do not modify ResultsForUpdate");
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Do not modify ResultsForUpdate");
	}
	
	@Override
	public boolean retainAll(Collection<?> collection) {
		throw new UnsupportedOperationException("Do not modify ResultsForUpdate");
	}
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		throw new UnsupportedOperationException("Do not modify ResultsForUpdate");
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Do not modify ResultsForUpdate");
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
	
	@Override
	public void addOnChangeListener(OnChangeListener<SomeInstance> listener) {
		unfilteredResultSet.addOnChangeListener(listener);
	}
	
	@Override
	public void removeOnChangeListener(OnChangeListener<SomeInstance> listener) {
		unfilteredResultSet.removeOnChangeListener(listener);
	}
	
	@Override
	public void onAdd(SomeInstance instance) {
		if (filter.needsUpdate(instance))
			filteredIndex.add(instance);
	}
	
	@Override
	public void onRemove(SomeInstance instance) {
		filteredIndex.remove(instance);
	}
}
