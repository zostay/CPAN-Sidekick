package com.qubling.sidekick.model;

import java.util.ArrayList;
import java.util.Collection;

public interface ResultSet<SomeInstance extends Instance<SomeInstance>> extends Collection<SomeInstance>, Iterable<SomeInstance>  {
	
	public interface Remap<FromInstance extends Instance<FromInstance>, ToInstance extends Instance<ToInstance>> {
		public Collection<ToInstance> map(FromInstance instance);
	}
	
	public abstract <OtherInstance extends Instance<OtherInstance>> void addRemap(
	        ResultSet<OtherInstance> others,
	        Remap<OtherInstance, SomeInstance> map);

	public abstract SomeInstance get(String key);

	public abstract SomeInstance get(int index);

	public abstract int getTotalSize();

	public abstract ArrayList<SomeInstance> toArrayList();
}