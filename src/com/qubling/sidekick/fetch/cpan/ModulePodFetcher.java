package com.qubling.sidekick.fetch.cpan;

import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.model.Model;

public class ModulePodFetcher extends CPANDirectFetcher<Module> {

	public ModulePodFetcher(Model<Module> model) {
	    super(model, FetchSection.MODULE_POD);
    }

	@Override
    public boolean needsUpdate(Module module) {
		return module.getRawPod() == null;
    }
    
	@Override
    protected String getRemainderUrl(Module module) {
	    if (module.getName().contains("/")) {
	        return module.getAuthorPauseId() + "/"
	             + module.getReleaseName() + "-"
	             + module.getRelease().getVersion() + "/"
	             + module.getName();
	    }
	    else {
	        return module.getKey();
	    }
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
