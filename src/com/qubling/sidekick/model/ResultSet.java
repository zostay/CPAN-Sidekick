package com.qubling.sidekick.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResultSet<SomeInstance extends Instance<SomeInstance>> implements Collection<SomeInstance>, Iterable<SomeInstance> {
	public interface Remap<FromInstance extends Instance<FromInstance>, ToInstance extends Instance<ToInstance>> {
		public Collection<ToInstance> map(FromInstance instance);
	}
	
	private Map<String, SomeInstance> results;
	private Map<Integer, String> resultIndex;
	private int totalSize = -1;
	
	public ResultSet() {
		results = new LinkedHashMap<String, SomeInstance>();
		resultIndex = new HashMap<Integer, String>();
	}
	
	public ResultSet(ArrayList<SomeInstance> loadedResults) {
		this();
		addAll(loadedResults);
	}
	
	@Override
	public boolean add(SomeInstance instance) {		
		if (results.put(instance.getKey(), instance) == null) {
			resultIndex.put(results.size(), instance.getKey());
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean addAll(Collection<? extends SomeInstance> instances) {
		boolean modified = false;
		
		for (SomeInstance instance : instances) {
			modified = results.put(instance.getKey(), instance) == null;
			if (modified) {
				resultIndex.put(results.size(), instance.getKey());
			}
		}
		
		return modified;
	}
	
	public <OtherInstance extends Instance<OtherInstance>> void addRemap(ResultSet<OtherInstance> others, Remap<OtherInstance, SomeInstance> map) {
		for (OtherInstance otherInstance : others) {
			addAll(map.map(otherInstance));
		}
	}
	
	@Override
	public void clear() {
		resultIndex.clear();
		results.clear();
	}
	
	protected void reindex() {
		resultIndex.clear();
		
		int i = 0;
		for (SomeInstance instance : results.values()) {
			resultIndex.put(i++, instance.getKey());
		}
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
				results.remove(pair.getKey());
				modified = true;
			}
		}
		
		if (modified) {
			reindex();
		}
		
		return modified;
	}
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean modified = false;
		
		for (Object o : collection) {
			if (o instanceof Instance) {
				Instance<?> instance = (Instance<?>) o;
				if (instance.equals(results.get(instance.getKey()))) {
					modified |= results.remove(instance.getKey()) != null;
				}
			}
		}
		
		if (modified) {
			reindex();
		}
		
		return modified;
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof Instance) {
			Instance<?> instance = (Instance<?>) o;
			if (!instance.equals(results.get(instance.getKey()))) 
				return false;
			
			Instance<SomeInstance> removed = results.remove(instance.getKey());
			if (removed != null) {
				reindex();
				return true;
			}
		}
		
		return false;
	}
	
	public SomeInstance get(String key) {
		return results.get(key);
	}
	
	public SomeInstance get(int index) {
		if (index >= results.size()) return null;
		String key = resultIndex.get(index);
		return results.get(key);
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
		return results.values().toArray();
	}
	
	@Override
	public <OtherInstance> OtherInstance[] toArray(OtherInstance[] array) {
		return results.values().toArray(array);
	}

	@Override
    public Iterator<SomeInstance> iterator() {
		return results.values().iterator();
    }
	
	public ArrayList<SomeInstance> toArrayList() {
		ArrayList<SomeInstance> arrayList = new ArrayList<SomeInstance>(results.size());
		arrayList.addAll(this);
		return arrayList;
	}
}
