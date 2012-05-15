package com.qubling.sidekick.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResultSet<SomeInstance extends Instance> implements Collection<SomeInstance>, Iterable<SomeInstance> {
	public interface Remap<FromInstance extends Instance, ToInstance extends Instance> {
		public Collection<ToInstance> map(FromInstance instance);
	}
	
	public interface OnInstanceUpdated<SomeInstance extends Instance> {
		public void notifyInstanceUpdated(ResultSet<SomeInstance> results, SomeInstance instance);
	}
	
	public interface OnChanged<SomeInstance extends Instance> {
		public void notifyChanged(ResultSet<SomeInstance> results);
	}
	
	private Map<String, SomeInstance> results;
	
	private Collection<OnChanged<SomeInstance>> onChangedListeners = new HashSet<OnChanged<SomeInstance>>();
	private Collection<OnInstanceUpdated<SomeInstance>> onInstanceUpdatedListeners = new HashSet<OnInstanceUpdated<SomeInstance>>();
	
	public ResultSet() {
		results = new LinkedHashMap<String, SomeInstance>();
	}
	
	@Override
	public boolean add(SomeInstance instance) {
		results.put(instance.getKey(), instance);
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends SomeInstance> instances) {
		boolean modified = false;
		
		for (SomeInstance instance : instances) {
			modified = true;
			results.put(instance.getKey(), instance);
		}
		
		return modified;
	}
	
	public <OtherInstance extends Instance> void addRemap(ResultSet<OtherInstance> others, Remap<OtherInstance, SomeInstance> map) {
		for (OtherInstance otherInstance : others) {
			addAll(map.map(otherInstance));
		}
	}
	
	@Override
	public void clear() {
		results.clear();
	}
	
	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean modified = false;
		
		HashSet<String> keepKeys = new HashSet<String>();
		for (Object o : collection) {
			if (o instanceof Instance) {
				Instance instance = (Instance) o;
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
		
		return modified;
	}
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean modified = false;
		
		for (Object o : collection) {
			if (o instanceof Instance) {
				Instance instance = (Instance) o;
				if (instance.equals(results.get(instance.getKey()))) {
					modified |= results.remove(instance.getKey()) != null;
				}
			}
		}
		
		return modified;
	}
	
	@Override
	public boolean remove(Object o) {
		if (o instanceof Instance) {
			Instance instance = (Instance) o;
			if (!instance.equals(results.get(instance.getKey()))) return false;
			return results.remove(instance.getKey()) != null;
		}
		
		return false;
	}
	
	public SomeInstance get(String key) {
		return results.get(key);
	}
	
	@Override
	public boolean contains(Object o) {
		if (o instanceof Instance) {
			Instance instance = (Instance) o;
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
				Instance instance = (Instance) o;
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
