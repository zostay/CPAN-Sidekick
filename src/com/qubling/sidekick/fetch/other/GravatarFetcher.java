package com.qubling.sidekick.fetch.other;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.qubling.sidekick.fetch.AbstractFetcher;
import com.qubling.sidekick.fetch.SerialUpdateFetcher;
import com.qubling.sidekick.fetch.UpdateFetcher;
import com.qubling.sidekick.model.Gravatar;
import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.search.ResultSet;
import com.qubling.sidekick.search.ResultsForUpdate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class GravatarFetcher extends AbstractFetcher<Gravatar> implements UpdateFetcher<Gravatar> {
	
	private float gravatarDpSize;
	private int timeoutAbsolute;
    
    private static final Pattern RESIZE_GRAVATAR_PATTERN = Pattern.compile("([?&])s=[0-9]+\\b");
    private static final int TIMEOUT_CONNECTION = 2000;
    private static final int TIMEOUT_SOCKET = 3000;
    
    public static final int DEFAULT_TIMEOUT_ABSOLUTE = 3100;
    
    public GravatarFetcher(Model<Gravatar> model, float gravatarDpSize, int timeoutAbsolute) {
    	super(model);
    	
    	this.gravatarDpSize = gravatarDpSize;
    	this.timeoutAbsolute = timeoutAbsolute;
    }
    
    public GravatarFetcher(Model<Gravatar> model, float gravatarDpSize) {
    	this(model, gravatarDpSize, DEFAULT_TIMEOUT_ABSOLUTE);
    }
    
    @Override
    public boolean needsUpdate(Gravatar gravatar) {
//    	Log.d("GravatarFetcher", "Needs update? " + (gravatar.getBitmap() == null));
    	return gravatar.getBitmap() == null;
    }
    
	public float getGravatarDpSize() {
    	return gravatarDpSize;
    }

	public int getTimeoutAbsolute() {
    	return timeoutAbsolute;
    }

	@Override
    protected void execute() {
//		Log.d("GravatarFetcher", "START execute()");
			
		ResultSet<Gravatar> inputResults = getResultSet();
		
		for (Gravatar gravatar : inputResults) {
			try {
				Bitmap bitmap = fetchBitmap(gravatar.getUrl());
	            gravatar.setBitmap(bitmap);
//	            Log.d("GravatarFetcher", "Fetched " + gravatar.getUrl());
			}
			catch (RuntimeException e) {
				Log.e("GravatarFetcher", "error fetching Gravatar", e);
			}
		}
		
//		Log.d("GravatarFetcher", "END execute()");
    }
	
	@Override
	public void setIncomingResultSet(ResultsForUpdate<Gravatar> inputResults) {
		setResultSet(inputResults);
	}

    private Bitmap fetchBitmap(String gravatarURL) {
    	Context context = getContext();

        // Calculate the pixel size of the Gravatar
        int gravatarPixelSize = Math.min(
                (int) (gravatarDpSize * context.getResources().getDisplayMetrics().density + 0.5f),
                512);

        Matcher resizeGravatarMatcher = RESIZE_GRAVATAR_PATTERN.matcher(gravatarURL);
        final String resizedGravatarURL = resizeGravatarMatcher.replaceFirst("$1s=" + gravatarPixelSize);

        try {

        	// Make sure we don't get stuck waiting for a Gravatar
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_CONNECTION);
            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_SOCKET);

            // Prepare the request
//            Log.d("AuthorByDistributionSearch", "Gravatar: " + resizedGravatarURL);
            HttpClient httpClient = getHttpClient();
            final HttpGet req = new HttpGet(resizedGravatarURL);
            req.setParams(httpParams);
            
            // Start the absolute timer for the request
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
				@Override
				public void run() {
					Log.e("GravatarFetcher", "Gravatar took too long, aborting fetch: " + resizedGravatarURL);
					req.abort();
				}
			}, timeoutAbsolute);
            
            // Do the request
            HttpResponse res = httpClient.execute(req);

            // Get the response content
            HttpEntity entity = res.getEntity();
            InputStream content = entity.getContent();
            Bitmap gravatarBitmap = BitmapFactory.decodeStream(content);
            return gravatarBitmap;
        }

        catch (IOException e) {
            Log.e("GravatarFetcher", "Error loading Gravatar: " + e);
            return null;
        }

        // During testing, I sometimes get an IllegalStateException: Connection is not open.
        // This is a stupid exception and nearly any illegal state exception we can ignore by
        // just not loading the Gravatar. The bitmap is not *that* important.
        catch (IllegalStateException e) {
        	Log.e("GravatarFetcher", "Error fetching Gravatar: " + e);
        	return null;
        }
    }
    
    public String toString() {
    	return getModel() + ":GravatarFetch(" + gravatarDpSize + ";" + getResultSet() + ")";
    }

	@Override
	public SerialUpdateFetcher<Gravatar> thenDoFetch(UpdateFetcher<Gravatar> fetcher) {
		return super.thenDoFetch(fetcher);
	}
}
