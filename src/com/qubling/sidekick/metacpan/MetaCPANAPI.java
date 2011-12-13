package com.qubling.sidekick.metacpan;

public abstract class MetaCPANAPI<Params, Progress, Result> extends RemoteAPI<Params, Progress, Result> {

	public static final String METACPAN_API_URL = "http://api.metacpan.org";

	public MetaCPANAPI(HttpClientManager clientManager) {
		super(clientManager);
	}
}
