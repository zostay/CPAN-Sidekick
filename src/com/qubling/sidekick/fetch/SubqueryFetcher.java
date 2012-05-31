package com.qubling.sidekick.fetch;

import java.util.Collection;

import com.qubling.sidekick.instance.Instance;
import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.search.ResultSet;
import com.qubling.sidekick.search.Results;
import com.qubling.sidekick.search.ResultsForUpdate;

import android.util.Log;

public class SubqueryFetcher<SomeInstance extends Instance<SomeInstance>, ForeignInstance extends Instance<ForeignInstance>> 
	extends AbstractFetcher<SomeInstance> 
	implements UpdateFetcher<SomeInstance> {
	
	private Results.Remap<SomeInstance, ForeignInstance> remapper;
	private UpdateFetcher<ForeignInstance> fetcher;
	
	public SubqueryFetcher(Model<SomeInstance> model, UpdateFetcher<ForeignInstance> fetcher, Results.Remap<SomeInstance, ForeignInstance> remapper) {
		super(model);
		
		this.fetcher = fetcher;
		this.remapper = remapper;
	}
	
	@Override
	public boolean needsUpdate(SomeInstance instance) {
		Collection<ForeignInstance> others = remapper.map(instance);
		for (ForeignInstance other : others) {
			if (fetcher.needsUpdate(other))
				return true;
		}
		
		return false;
	}
	
	@Override
	public void setIncomingResultSet(ResultsForUpdate<SomeInstance> inputResults) {
		setResultSet(inputResults);
	}

	@Override
	protected void execute() {
		Log.d("SubqueryFetcher", "START execute()");
		
		ResultSet<ForeignInstance> inputResults = new Results<ForeignInstance>();
		inputResults.addRemap(getResultSet(), remapper);
		fetcher.setIncomingResultSet(
				new ResultsForUpdate<ForeignInstance>(fetcher, inputResults));
		fetcher.run();
		
		Log.d("SubqueryFetcher", "END execute()");
	}
	
	@Override
	public String toString() {
		return getModel() + ":SubqueryFetcher(" + fetcher + ";" + getResultSet() + ")";
	}

	@Override
	public SerialUpdateFetcher<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance> fetcher) {
		return super.thenDoFetch(fetcher);
	}
}
