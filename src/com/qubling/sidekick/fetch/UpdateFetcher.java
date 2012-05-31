package com.qubling.sidekick.fetch;

import com.qubling.sidekick.model.Instance;
import com.qubling.sidekick.model.ResultsForUpdate;

public interface UpdateFetcher<SomeInstance extends Instance<SomeInstance>> extends Fetcher<SomeInstance> {
	public void setIncomingResultSet(ResultsForUpdate<SomeInstance> input);
	
	public boolean needsUpdate(SomeInstance instance);

    public SerialUpdateFetcher<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance> updateFetcher);
}
