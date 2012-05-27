package com.qubling.sidekick.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import com.qubling.sidekick.job.ActivityUiCallable;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

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
		Log.d("AbstractFetcher", "setResultSet() " + inputResults);
		this.results = inputResults;
	}
	
	public ResultSet<SomeInstance> getResultSet() {
		if (results == null)
			results = new Results<SomeInstance>();
		return results;
	}
	
	protected Context getContext() {
		return model.getSchema().getContext();
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
	
	public final Void call() throws Exception {
		try {
			Log.d("AbstractFetcher", "START call()");
			execute();
			notifyOnFinished();
			Log.d("AbstractFetcher", "END call()");
			return null;
		}
		catch (RuntimeException e) {
			Log.e("AbstractFetcher", "Error while executing fetch.", e);
			throw e;
		}
		catch (Exception e) {
			Log.e("AbstractFetcher", "Error while executing fetch.", e);
			throw e;
		}
	}
	
	protected abstract void execute() throws Exception;

    protected HttpClient getHttpClient() {
        return getSchema().getHttpClient();
    }

    public static String getCharset(HttpResponse res) {
        HttpEntity entity = res.getEntity();

        String charset;
        String contentType = entity.getContentType().getValue();
        int charsetIndex = contentType.indexOf("charset=");
        if (charsetIndex >= 0) {
            int endingIndex = contentType.indexOf(";", charsetIndex + 8);
            if (endingIndex >= 0) {
                charset = contentType.substring(charsetIndex + 8, endingIndex);
            }
            else {
                charset = contentType.substring(charsetIndex + 8);
            }
        }
        else {
            charset = "UTF-8";
        }

        return charset;
    }

    public static String slurpContent(HttpResponse res) throws IOException {
        HttpEntity entity = res.getEntity();
        String charset = getCharset(res);

        InputStreamReader contentReader = new InputStreamReader(entity.getContent(), charset);
        char[] buf = new char[1000];
        StringBuilder contentStr = new StringBuilder();
        int readLength;
        while ((readLength = contentReader.read(buf)) > -1) {
            contentStr.append(buf, 0, readLength);
        }

        return contentStr.toString();
    }
    
    @Override
    public Consequence<SomeInstance> whenFinishedNotify(final OnFinished<SomeInstance> listener) {
    	Callable<Void> consequence = new Callable<Void>() {
    		@Override
    		public Void call() throws Exception {
    			listener.onFinishedFetch(AbstractFetcher.this, getResultSet());
    			return null;
    		}
    		
    		@Override
    		public String toString() {
    			return "whenFinishedNotify:ConsequentCaller(" + listener + ")";
    		}
		};
		
		return setupConsequence(consequence);
    }
    
    private Consequence<SomeInstance> setupConsequence(Callable<Void> consequence) {
		if (this instanceof Consequence<?>) {
			return ((Consequence<SomeInstance>) this).addConsequence(consequence);
		}
		else {
			return new Consequence<SomeInstance>(getModel(), this);
		}
    }
    
    @Override
    public Consequence<SomeInstance> whenFinishedNotifyUi(final Activity activity, final OnFinished<SomeInstance> listener) {
    	Callable<Void> consequence = new ActivityUiCallable<Void>() {
    		@Override
    		public Void call() {
    			listener.onFinishedFetch(AbstractFetcher.this, AbstractFetcher.this.getResultSet());
    			return null;
    		}

			@Override
            public Activity getActivity() {
	            return activity;
            }
			
			@Override
			public String toString() {
				return "whenFinishedNotifyUi:ConsequentCallable(" + listener + ")";
			}
		};
		
		return setupConsequence(consequence);
    }
    
    @Override
    public Consequence<SomeInstance> thenDoFetch(final UpdateFetcher<SomeInstance> updateFetcher) {
    	return setupConsequence(updateFetcher);
    }
    
    @Override
    public String toString() {
    	return model + ":AbstractFetcher()";
    }
}
