package com.qubling.sidekick.model;

public class Author extends Instance {
	private String pauseId;
	private Gravatar gravatar;
	
	public Author(Model<Author> model, String pauseId) {
		super(model);
		
		this.pauseId = pauseId;
	}

	@Override
	public String getKey() {
		return pauseId;
	}

	public String getPauseId() {
    	return pauseId;
    }

	public String getGravatarUrl() {
    	return gravatar == null ? null : gravatar.getUrl();
    }

	public void setGravatarUrl(String gravatarUrl) {
		gravatar = getModel().getSchema().getGravatarModel().acquireInstance(gravatarUrl);
    }
	
	public Gravatar getGravatar() {
    	return gravatar;
    }

	public void setGravatar(Gravatar gravatar) {
    	this.gravatar = gravatar;
    }
}
