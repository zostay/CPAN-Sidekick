package com.qubling.sidekick.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import com.qubling.sidekick.instance.Instance;

public interface ResultSet<SomeInstance extends Instance<SomeInstance>> extends Collection<SomeInstance>, Iterable<SomeInstance>  {
	
	public interface Remap<FromInstance extends Instance<FromInstance>, ToInstance extends Instance<ToInstance>> {
		public Collection<ToInstance> map(FromInstance instance);
	}
	
	public interface OnChangeListener<SomeInstance extends Instance<SomeInstance>> {
		public void onAdd(SomeInstance instance);
		public void onRemove(SomeInstance instance);
	}
	
	public <OtherInstance extends Instance<OtherInstance>> void addRemap(
	        ResultSet<OtherInstance> others,
	        Remap<OtherInstance, SomeInstance> map);

	public SomeInstance get(String key);

	public SomeInstance get(int index);

	public int getTotalSize();
	public void setTotalSize(int totalSize) throws UnsupportedOperationException;

	public ArrayList<SomeInstance> toArrayList();
	
	public void addOnChangeListener(OnChangeListener<SomeInstance> listener);
	public void removeOnChangeListener(OnChangeListener<SomeInstance> listener);
	
	public void sort(Comparator<SomeInstance> comparator);
}