package com.qubling.sidekick.fetch;

import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.HttpClient;

import com.qubling.sidekick.instance.Instance;
import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.search.ResultSet;
import com.qubling.sidekick.search.Results;
import com.qubling.sidekick.search.ResultsForUpdate;
import com.qubling.sidekick.search.Schema;

import android.app.Activity;
import android.content.Context;

public abstract class AbstractFetcher<SomeInstance extends Instance<SomeInstance>> implements Fetcher<SomeInstance> {
	public static class OnFinishedUi<SomeInstance extends Instance<SomeInstance>> implements OnFinished<SomeInstance> {
		private final Activity activity;
		private final OnFinished<SomeInstance> listener;
		
		public OnFinishedUi(Activity activity, OnFinished<SomeInstance> listener) {
			this.activity = activity;
			this.listener = listener;
		}
		
		public void onFinishedFetch(final Fetcher<SomeInstance> fetcher, final ResultSet<SomeInstance> results) {
			activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					listener.onFinishedFetch(fetcher, results);
				}
			});
		}
	}
	
	private Set<OnFinished<SomeInstance>> onFinishedListeners;
	private ResultSet<SomeInstance> results;
	private Model<SomeInstance> model;
	
	public AbstractFetcher(Model<SomeInstance> model) {
		this.onFinishedListeners = new HashSet<OnFinished<SomeInstance>>();
		this.model = model;
	}
	
	public Model<SomeInstance> getModel() {
		return model;
	}
	
	public Schema getSchema() {
		return model.getSchema();
	}
	
	protected void setResultSet(ResultSet<SomeInstance> inputResults) {
//		Log.d("AbstractFetcher", "setResultSet() " + inputResults);
		this.results = inputResults;
	}
	
	public ResultSet<SomeInstance> getResultSet() {
		if (results == null)
			results = new Results<SomeInstance>();
		return results;
	}
	
	protected Context getContext() {
		return model.getSchema().getActivity();
	}
	
	public void addOnFinishedListener(OnFinished<SomeInstance> onCompleteListener) {
    	onFinishedListeners.add(onCompleteListener);
    }
	
	public void addOnFinishedListenerUi(Activity activity, OnFinished<SomeInstance> listener) {
		onFinishedListeners.add(new OnFinishedUi<SomeInstance>(activity, listener));
	}

	public void removeOnFinishedListener(OnFinished<SomeInstance> onCompleteListener) {
    	onFinishedListeners.remove(onCompleteListener);
    }
	
	public void notifyOnFinished() {
		ResultSet<SomeInstance> results = getResultSet();
		for (OnFinished<SomeInstance> listener : onFinishedListeners) {
			listener.onFinishedFetch(this, results);
		}
	}
	
	public final void run() {
//		Log.d("AbstractFetcher", "START call()");
		execute();
		notifyOnFinished();
//		Log.d("AbstractFetcher", "END call()");
	}
	
	protected abstract void execute();

    protected HttpClient getHttpClient() {
        return getSchema().getHttpClient();
    }
    
    @SuppressWarnings("unchecked")
    protected SerialUpdateFetcher<SomeInstance> thenDoFetch(UpdateFetcher<SomeInstance> updateFetcher) {
    	SerialUpdateFetcher<SomeInstance> serialFetcher = new SerialUpdateFetcher<SomeInstance>(getModel(), (UpdateFetcher<SomeInstance>) this);
    	updateFetcher.setIncomingResultSet(
    			new ResultsForUpdate<SomeInstance>(updateFetcher, getResultSet()));
    	return serialFetcher.thenDoFetch(updateFetcher);
    }
    
    @Override
    public String toString() {
    	return model + ":AbstractFetcher()";
    }
}
