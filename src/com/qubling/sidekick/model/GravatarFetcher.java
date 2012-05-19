package com.qubling.sidekick.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
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
    
    public static final int DEFAULT_TIMEOUT_ABSOLUTE = 10000;
    
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
    	return gravatar.getBitmap() == null;
    }
    
	public float getGravatarDpSize() {
    	return gravatarDpSize;
    }

	public int getTimeoutAbsolute() {
    	return timeoutAbsolute;
    }

	@Override
    protected ResultSet<Gravatar> execute() {
		ResultSet<Gravatar> inputResults = getResultSet();
		
		for (Gravatar gravatar : inputResults) {
            Bitmap bitmap = fetchBitmap(gravatar.getUrl());
            gravatar.setBitmap(bitmap);
		}
		
		return inputResults;
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
        String resizedGravatarURL = resizeGravatarMatcher.replaceFirst("$1s=" + gravatarPixelSize);

        try {

        	// Make sure we don't get stuck waiting for a Gravatar
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_CONNECTION);
            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_SOCKET);

            // Do the request
//            Log.d("AuthorByDistributionSearch", "Gravatar: " + resizedGravatarURL);
            HttpGet req = new HttpGet(resizedGravatarURL);
            req.setParams(httpParams);
            HttpResponse res = getHttpClient().execute(req);

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

	@Override
    public UpdateFetcher<Gravatar> thenAfterUpdateDoFetch(UpdateFetcher<Gravatar> fetcher) {
	    thenDoFetch(fetcher);
	    return this;
    }

	@Override
    public UpdateFetcher<Gravatar> whenUpdateFinishedNotifyUi(Activity activity, OnFinished<Gravatar> listener) {
	    whenFinishedNotifyUi(activity, listener);
	    return this;
    }

	@Override
    public UpdateFetcher<Gravatar> whenUpdateFinishedNotify(OnFinished<Gravatar> listener) {
	    whenFinishedNotify(listener);
	    return this;
    }
}
