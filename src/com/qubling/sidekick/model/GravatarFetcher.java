package com.qubling.sidekick.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class GravatarFetcher extends Fetcher<Gravatar> {
	private float gravatarDpSize;
	private int timeoutAbsolute;
	
	private List<String> gravatarUrls;
    
    private static final Pattern RESIZE_GRAVATAR_PATTERN = Pattern.compile("([?&])s=[0-9]+\\b");
    private static final int TIMEOUT_CONNECTION = 2000;
    private static final int TIMEOUT_SOCKET = 3000;
    
    public static final int DEFAULT_TIMEOUT_ABSOLUTE = 10000;
    
    public GravatarFetcher(List<String> gravatarUrls, float gravatarDpSize, int timeoutAbsolute) {
    	this.gravatarUrls = gravatarUrls;
    	this.gravatarDpSize = gravatarDpSize;
    	this.timeoutAbsolute = timeoutAbsolute;
    }
    
    public GravatarFetcher(List<String> gravatarUrls, float gravatarDpSize) {
    	this(gravatarUrls, gravatarDpSize, DEFAULT_TIMEOUT_ABSOLUTE);
    }
    
	public float getGravatarDpSize() {
    	return gravatarDpSize;
    }

	public int getTimeoutAbsolute() {
    	return timeoutAbsolute;
    }

	@Override
    protected ResultSet<Gravatar> execute() {
		ResultSet<Gravatar> results = getResultSet();
		
		for (String url : gravatarUrls) {
            Bitmap bitmap = fetchBitmap(url);
            Gravatar gravatar = getSchema().getGravatarModel().acquireInstance(url);
            gravatar.setBitmap(bitmap);
            results.add(gravatar);
		}
		
		return results;
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
}
