package com.qubling.sidekick.search;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qubling.sidekick.instance.Instance;

public class Results<SomeInstance extends Instance<SomeInstance>> 
		extends AbstractCollection<SomeInstance> 
		implements ResultSet<SomeInstance> {
	
	private Map<String, SomeInstance> results;
	private List<SomeInstance> resultIndex;
	private int totalSize = -1;
	
	private Collection<OnChangeListener<SomeInstance>> onChangeListeners = new HashSet<OnChangeListener<SomeInstance>>();
	
	public Results() {
		results = new LinkedHashMap<String, SomeInstance>();
		resultIndex = new ArrayList<SomeInstance>();
	}
	
	public Results(SomeInstance singleton) {
		this();
		add(singleton);
	}
	
	public Results(ArrayList<SomeInstance> loadedResults) {
		this();
		addAll(loadedResults);
	}
	
	@Override
	public boolean add(SomeInstance instance) {		
		if (results.put(instance.getKey(), instance) == null) {
			resultIndex.add(instance);
			notifyOnAdd(instance);
			return true;
		}
		
		if (resultIndex.size() > results.size())
			throw new IllegalStateException("invariant violated: index is larger than base collection");
		
		return false;
	}
	
	@Override
    public <OtherInstance extends Instance<OtherInstance>> void addRemap(ResultSet<OtherInstance> others, Remap<OtherInstance, SomeInstance> map) {
		for (OtherInstance otherInstance : others) {
			addAll(map.map(otherInstance));
		}
	}
	
	@Override
	public void clear() {
		for (SomeInstance instance : resultIndex) {
			notifyOnRemove(instance);
		}
		
		resultIndex.clear();
		results.clear();
	}
	
	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean modified = false;
		
		HashSet<String> keepKeys = new HashSet<String>();
		for (Object o : collection) {
			if (o instanceof Instance) {
				Instance<?> instance = (Instance<?>) o;
				if (instance.equals(results.get(instance.getKey()))) {
					keepKeys.add(instance.getKey());
				}
			}
		}
		
		for (Map.Entry<String, SomeInstance> pair : results.entrySet()) {
			if (!keepKeys.contains(pair.getKey())) {
				remove(pair.getValue());
				modified = true;
			}
		}
		
		return modified;
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof Instance) {
			Instance<?> instance = (Instance<?>) o;
			
			// This is probably not possible... but just in case
			if (!instance.equals(results.get(instance.getKey())))
				return false;
			
			SomeInstance removed = results.remove(instance.getKey());
			if (removed != null) {
				resultIndex.remove(o);
				notifyOnRemove(removed);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
    public SomeInstance get(String key) {
		return results.get(key);
	}
	
	@Override
    public SomeInstance get(int index) {
		if (index >= results.size()) return null;
		return resultIndex.get(index);
	}
	
	@Override
	public boolean contains(Object o) {
		if (o instanceof Instance) {
			Instance<?> instance = (Instance<?>) o;
			String key = instance.getKey();
			
			if (!results.containsKey(key) || !instance.equals(results.get(key))) {
				return false;
			}
		}
		else {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean containsAll(Collection<?> collection) {
		for (Object o : collection) {
			if (o instanceof Instance) {
				Instance<?> instance = (Instance<?>) o;
				String key = instance.getKey();
				
				if (!results.containsKey(key) || !instance.equals(results.get(key))) {
					return false;
				}
			}
			else {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int size() {
		return results.size();
	}
	
	@Override
    public int getTotalSize() {
    	return totalSize > -1 ? totalSize : results.size();
    }

    public void setTotalSize(int totalSize) {
    	this.totalSize = totalSize;
    }

	@Override
	public boolean isEmpty() {
		return results.isEmpty();
	}
	
	@Override
	public Object[] toArray() {
		return resultIndex.toArray();
	}
	
	@Override
    public <OtherInstance> OtherInstance[] toArray(OtherInstance[] array) {
		return resultIndex.toArray(array);
	}

	@Override
    public Iterator<SomeInstance> iterator() {
		return resultIndex.iterator();
    }
	
	@Override
    public ArrayList<SomeInstance> toArrayList() {
		ArrayList<SomeInstance> arrayList = new ArrayList<SomeInstance>(resultIndex.size());
		arrayList.addAll(resultIndex);
		return arrayList;
	}
	
	protected List<SomeInstance> allResults() {
		return Collections.unmodifiableList(resultIndex);
	}
	
	@Override
	public void addOnChangeListener(OnChangeListener<SomeInstance> listener) {
		onChangeListeners.add(listener);
	}
	
	@Override
	public void removeOnChangeListener(OnChangeListener<SomeInstance> listener) {
		onChangeListeners.remove(listener);
	}
	
	protected void notifyOnAdd(SomeInstance instance) {
		for (OnChangeListener<SomeInstance> listener : onChangeListeners) {
			listener.onAdd(instance);
		}
	}
	
	protected void notifyOnRemove(SomeInstance instance) {
		for (OnChangeListener<SomeInstance> listener : onChangeListeners) {
			listener.onRemove(instance);
		}
	}
	
	@Override
	public void sort(Comparator<SomeInstance> comparator) {
	    Collections.sort(resultIndex, comparator);
	}
	
	@Override
	public int indexOf(SomeInstance needle) {
	    int i = 0;
	    for (SomeInstance instance : this) {
	        if (instance.getKey().equals(needle.getKey()))
	            return i;
	        i++;
	    }
	    return -1;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("Results(");
		for (int i = 0; i < size() && i < 3; i++) {
			result.append(get(i));
			result.append(",");
		}
		
		if (size() > 3)
			result.append("...");
		
		result.append(")");
		
		return result.toString();
	}
}
