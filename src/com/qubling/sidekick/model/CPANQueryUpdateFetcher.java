package com.qubling.sidekick.model;

public abstract class CPANQueryUpdateFetcher<SomeInstance extends Instance<SomeInstance>>
        extends CPANQueryFetcher<SomeInstance> implements
        UpdateFetcher<SomeInstance> {
	
	public CPANQueryUpdateFetcher(Model<SomeInstance> model, SearchSection searchSection, String searchTemplate) {
		super(model, searchSection, searchTemplate);
	}

	@Override
	public void setIncomingResultSet(ResultsForUpdate<SomeInstance> input) {
		setResultSet(input);
	}
	
	@Override
	public String toString() {
		return getModel() + ":CPANQueryFetcher(" + getSearchSection() + ";" + getSearchTemplate().hashCode() + ")";
	}

	@Override
	public SerialUpdateFetcher<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance> fetcher) {
		return super.thenDoFetch(fetcher);
	}

}
