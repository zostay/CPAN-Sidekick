package com.qubling.sidekick.model;

public class Module extends Instance {
	private String name;
	private String moduleAbstract; // keywords clashing with variable names? tsk, tsk... #NeedPerlSigils
	private String rawPod;
	private Release release;
	
	public Module(Model<Module> model, String name) {
		super(model);
		
		this.name = name;
	}
	
	@Override
	public String getKey() {
		return name;
	}

	public String getName() {
    	return name;
    }
	
	public String getAbstract() {
		return moduleAbstract;
	}
	
	public void setAbstract(String moduleAbstract) {
		this.moduleAbstract = moduleAbstract;
	}
	
	public String getRawPod() {
    	return rawPod;
    }

	public void setRawPod(String rawPod) {
    	this.rawPod = rawPod;
    }

	public String getReleaseName() {
		return release == null ? null : release.getName();
	}
	
	public void setReleaseName(String name) {
		release = getModel().getSchema().getReleaseModel().acquireInstance(name);
	}

	public Release getRelease() {
    	return release;
    }
	
	public String getAuthorPauseId() {
		return release == null ? null : release.getAuthorPauseId();
	}

	public Author getAuthor() {
    	return release == null ? null : release.getAuthor();
    }
}
