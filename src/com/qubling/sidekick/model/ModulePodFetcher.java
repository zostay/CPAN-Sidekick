package com.qubling.sidekick.model;

public class ModulePodFetcher extends CPANDirectFetcher<Module> {

	public ModulePodFetcher(Model<Module> model) {
	    super(model, FetchSection.MODULE_POD);
    }

	@Override
    public boolean needsUpdate(Module module) {
		return module.getRawPod() == null;
    }

	@Override
    public void consumeResponse(String content, Module module) {
		module.setRawPod(content);
    }

	@Override
	public String toString() {
		return getModel() + ":ModulePodFetcher(" + getResultSet() + ")";
	}
}
