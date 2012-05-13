package com.qubling.sidekick.model;

public abstract class Instance {
	private Model<? extends Instance> model;
	
	public Instance(Model<? extends Instance> model) {
		this.model = model;
	}
	
	public Model<? extends Instance> getModel() {
		return model;
	}

	public abstract String getKey();

	@Override
    public boolean equals(Object o) {
		if (this.getClass().equals(o.getClass())) {
			return getKey().equals(((Instance) o).getKey());
		}

		return false;
    }

	@Override
    public int hashCode() {
	    return getKey().hashCode();
    }
}
