package com.qubling.sidekick.search;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import com.qubling.sidekick.R;
import com.qubling.sidekick.fetch.Fetcher;
import com.qubling.sidekick.instance.Instance;
import com.qubling.sidekick.model.AuthorModel;
import com.qubling.sidekick.model.GravatarModel;
import com.qubling.sidekick.model.ModuleModel;
import com.qubling.sidekick.model.ReleaseModel;
import com.qubling.sidekick.search.Search.OnSearchActivity;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class Schema implements OnSearchActivity {
	private static final String METACPAN_API_USER_AGENT_SUFFIX = " (Android)";
	
	private static int schemaIdCounter = 0;
	
	private final GravatarModel gravatarModel;
	private final AuthorModel authorModel;
	private final ReleaseModel releaseModel;
	private final ModuleModel moduleModel;
	
	private int schemaId;
	private int runningSearches = 0;
	
	private Activity activity;
	private HttpClient httpClient;
	
	public Schema(Activity activity) {
		schemaId = ++schemaIdCounter;
		
		gravatarModel = new GravatarModel(this);
		authorModel = new AuthorModel(this);
		releaseModel = new ReleaseModel(this);
		moduleModel = new ModuleModel(this);
		
		this.activity = activity;
	}
	
    private void setupHttpClient() {
        try {
    		
    		String userAgent = activity.getString(R.string.app_name)
    				         + "/"
    				         + activity.getString(R.string.app_version)
    				         + METACPAN_API_USER_AGENT_SUFFIX;
    		
            httpClient = (HttpClient) Class.forName("android.net.http.AndroidHttpClient")
                .getMethod("newInstance", String.class).invoke(null, userAgent);
            Log.i("HttpClientManager", "Using AndroidHttpClient");
        }
        catch (Throwable t) {
        	Log.i("HttpClientManager", "Falling back to DefaultHttpClient");
            httpClient = new DefaultHttpClient();
        }
    }
    
    public void closeHttpClient() {
    	if (httpClient instanceof AndroidHttpClient) {
    		((AndroidHttpClient) httpClient).close();
    	}
    	
    	httpClient = null;
    }
	
	public GravatarModel getGravatarModel() {
		return gravatarModel;
	}
	
	public AuthorModel getAuthorModel() {
    	return authorModel;
    }

	public ReleaseModel getReleaseModel() {
    	return releaseModel;
    }

	public ModuleModel getModuleModel() {
    	return moduleModel;
    }
	
	public Activity getActivity() {
    	return activity;
    }

	public HttpClient getHttpClient() {
    	return httpClient;
    }
	
	public <SomeInstance extends Instance<SomeInstance>> Search<SomeInstance> doFetch(Fetcher<SomeInstance> fetcher, Fetcher.OnFinished<SomeInstance> listener) {
		Search<SomeInstance> search = new Search<SomeInstance>(activity, fetcher, listener);
		search.addOnSearchActivityListener(this);
		return search;
	}
	
	@Override
	public synchronized void onSearchStart() {
//		Log.d("Schema", "onSearchStart()");
		if (runningSearches == 0) setupHttpClient();
		runningSearches++;
	}
	
	@Override
	public synchronized void onSearchComplete() {
//		Log.d("Schema", "onSearchComplete()");
		runningSearches--;
		if (runningSearches == 0) closeHttpClient();
	}
	
	@Override
	public String toString() {
		return "Session #" + schemaId;
	}
}
