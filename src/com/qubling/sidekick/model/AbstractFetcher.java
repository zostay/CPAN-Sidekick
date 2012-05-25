package com.qubling.sidekick.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

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
	
	public final ResultSet<SomeInstance> call() throws Exception {
		try {
			Log.d("AbstractFetcher", "START call()");
			ResultSet<SomeInstance> results = execute();
			notifyOnFinished();
			Log.d("AbstractFetcher", "END call()");
			return results;
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
	
	protected abstract ResultSet<SomeInstance> execute() throws Exception;

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
    public Fetcher<SomeInstance> whenFinishedNotify(OnFinished<SomeInstance> listener) {
    	addOnFinishedListener(listener);
    	return this;
    }
    
    @Override
    public Fetcher<SomeInstance> whenFinishedNotifyUi(Activity activity, OnFinished<SomeInstance> listener) {
    	addOnFinishedListenerUi(activity, listener);
    	return this;
    }
    
    @Override
    public Fetcher<SomeInstance> thenDoFetch(final UpdateFetcher<SomeInstance> updateFetcher) {
    	addOnFinishedListener(new OnFinished<SomeInstance>() {
			
			@Override
			public void onFinishedFetch(Fetcher<SomeInstance> originFetcher, ResultSet<SomeInstance> results) {
				try {
					updateFetcher.setIncomingResultSet(
							new ResultsForUpdate<SomeInstance>(updateFetcher, results));
					updateFetcher.call();
				}
				catch (Exception e) {
					Log.e("Fetcher", "Error while executing followup fetcher.", e);
				}
			}
		});
    	
    	return this;
    }
}
