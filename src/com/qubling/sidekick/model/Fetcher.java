package com.qubling.sidekick.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import com.qubling.sidekick.api.HttpClientManager;

import android.content.Context;

public abstract class Fetcher<SomeInstance extends Instance> implements Callable<ResultSet<SomeInstance>>{
	public interface OnComplete<SomeInstance extends Instance> {
		public abstract void onComplete(Fetcher<SomeInstance> fetcher, ResultSet<SomeInstance> results);
	}
	
	private Set<OnComplete<SomeInstance>> onCompleteListeners;
	private ResultSet<SomeInstance> results;
	private Schema schema;
	
	public Fetcher() {
		this.onCompleteListeners = new HashSet<OnComplete<SomeInstance>>();
		this.results = new ResultSet<SomeInstance>();
	}
	
	public void setSchema(Schema schema) {
		this.schema = schema;
	}
	
	protected HttpClientManager getHttpClientManager() {
		return getSchema().getClientManager();
	}
	
	public ResultSet<SomeInstance> getResultSet() {
		return results;
	}
	
	public Schema getSchema() {
		return schema;
	}
	
	protected Context getContext() {
		return schema.getContext();
	}
	
	public ExecutorService getPreferredExecutor() {
		return getSchema().getJobExecutor();
	}
	
	public void addOnCompleteListener(OnComplete<SomeInstance> onCompleteListener) {
    	onCompleteListeners.add(onCompleteListener);
    }

	public void removeOnCompleteListener(OnComplete<SomeInstance> onCompleteListener) {
    	onCompleteListeners.remove(onCompleteListener);
    }
	
	public void notifyOnComplete() {
		ResultSet<SomeInstance> results = getResultSet();
		for (OnComplete<SomeInstance> listener : onCompleteListeners) {
			listener.onComplete(this, results);
		}
	}
	
	public final ResultSet<SomeInstance> call() throws Exception {
		ResultSet<SomeInstance> results = execute();
		notifyOnComplete();
		return results;
	}
	
	protected abstract ResultSet<SomeInstance> execute() throws Exception;

    protected HttpClient getHttpClient() {
        return getHttpClientManager().getClient();
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
}
