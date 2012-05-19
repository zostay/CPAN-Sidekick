package com.qubling.sidekick.model;

public interface UpdateFetcher<SomeInstance extends Instance<SomeInstance>> extends Fetcher<SomeInstance> {
	public void setIncomingResultSet(ResultsForUpdate<SomeInstance> input);
	
	public boolean needsUpdate(SomeInstance instance);
}
