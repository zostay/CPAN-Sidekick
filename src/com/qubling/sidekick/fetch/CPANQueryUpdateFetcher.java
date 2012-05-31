package com.qubling.sidekick.fetch;

import com.qubling.sidekick.model.Instance;
import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.model.ResultsForUpdate;

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
