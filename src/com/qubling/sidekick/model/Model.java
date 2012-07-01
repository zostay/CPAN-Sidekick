package com.qubling.sidekick.model;

import java.util.HashMap;
import java.util.Map;

import com.qubling.sidekick.instance.Instance;
import com.qubling.sidekick.search.Schema;


public abstract class Model<SomeInstance extends Instance<SomeInstance>> {
	private final Schema schema;
	private Map<String, SomeInstance> objectCache;

	public Model(Schema schema) {
		this.schema = schema;
		this.objectCache = new HashMap<String, SomeInstance>();
	}
	
	public Schema getSchema() {
    	return schema;
    }
	
	public void cache(SomeInstance instance) {
	    objectCache.put(instance.getKey(), instance);
	}
	
	protected abstract SomeInstance constructInstance(String key);
	
	public SomeInstance acquireInstance(String key) {
		SomeInstance instance = objectCache.get(key);
		
		if (instance == null) {
			instance = constructInstance(key);
			objectCache.put(key, instance);
		}
		
		return instance;
	}
}
