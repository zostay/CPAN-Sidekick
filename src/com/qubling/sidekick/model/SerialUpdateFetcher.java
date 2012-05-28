package com.qubling.sidekick.model;

import java.util.ArrayList;
import java.util.List;

public class SerialUpdateFetcher<SomeInstance extends Instance<SomeInstance>>
        extends AbstractFetcher<SomeInstance> implements
        UpdateFetcher<SomeInstance> {
	
	private List<UpdateFetcher<SomeInstance>> fetchers;
	
	public SerialUpdateFetcher(Model<SomeInstance> model, UpdateFetcher<SomeInstance> origin) {
		super(model);
		
		fetchers = new ArrayList<UpdateFetcher<SomeInstance>>();
		fetchers.add(origin);
	}

	@Override
	public void setIncomingResultSet(ResultsForUpdate<SomeInstance> input) {
		for (UpdateFetcher<SomeInstance> fetcher : fetchers) {
			fetcher.setIncomingResultSet(input);
		}
	}

	@Override
	public boolean needsUpdate(SomeInstance instance) {
		for (UpdateFetcher<SomeInstance> fetcher : fetchers) {
			if (fetcher.needsUpdate(instance))
				return true;
		}
		
		return false;
	}

	@Override
	protected void execute() {
		for (UpdateFetcher<SomeInstance> fetcher : fetchers) {
			fetcher.run();
		}
	}

	@Override
	public SerialUpdateFetcher<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance> fetcher) {
		fetchers.add(fetcher);
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < 3 && i < fetchers.size(); i++) {
			UpdateFetcher<SomeInstance> fetcher = fetchers.get(i);
			result.append(fetcher);
			result.append(",");
		}
		
		if (fetchers.size() > 3) result.append("...");
		
		return getModel() + ":SerialUpdateFetcher(" + result + ")";
	}
}
