package com.qubling.sidekick.model;

public abstract class Instance<SomeInstance extends Instance<SomeInstance>> implements Parcelable {
	private Model<SomeInstance> model;
	
	public Instance() { }
	
	public Instance(Model<SomeInstance> model) {
		this.model = model;
	}
	
	public Model<SomeInstance> getModel() {
		if (isAttachedToModel()) {
			return model;
		}
		else {
			throw new IllegalStateException("You must call attachToModel() at your earliest convenience after construction.");
		}
	}
	
	public void attachToModel(Model<SomeInstance> model) {
		this.model = model;
	}
	
	public boolean isAttachedToModel() {
		return this.model != null;
	}

	public abstract String getKey();

	@Override
    public boolean equals(Object o) {
		if (this.getClass().equals(o.getClass())) {
			return getKey().equals(((Instance<?>) o).getKey());
		}

		return false;
    }

	@Override
    public int hashCode() {
	    return getKey().hashCode();
    }
}
