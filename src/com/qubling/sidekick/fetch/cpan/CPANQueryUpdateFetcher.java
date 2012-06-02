package com.qubling.sidekick.fetch.cpan;

import com.qubling.sidekick.fetch.SerialUpdateFetcher;
import com.qubling.sidekick.fetch.UpdateFetcher;
import com.qubling.sidekick.instance.Instance;
import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.search.ResultsForUpdate;

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
	
	@Override
	public boolean shouldCompleteRequest() {
//		Log.d("CPANQueryUpdateFetcher", "getResultSet().size() = " + getResultSet().size());
		return getResultSet().size() > 0;
	}

}
