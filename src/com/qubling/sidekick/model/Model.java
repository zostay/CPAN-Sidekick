package com.qubling.sidekick.model;


public abstract class Model<SomeInstance extends Instance<SomeInstance>> {
	private final Schema schema;
	private ResultSet<SomeInstance> objectCache;

	public Model(Schema schema) {
		this.schema = schema;
	}
	
	public Schema getSchema() {
    	return schema;
    }
	
	protected abstract SomeInstance constructInstance(String key);
	
	public SomeInstance acquireInstance(String key) {
		SomeInstance instance = objectCache.get(key);
		
		if (instance == null) {
			instance = constructInstance(key);
		}
		
		return instance;
	}
}
