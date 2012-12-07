package com.qubling.sidekick.fetch.other;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Timer;
import java.util.TimerTask;

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
import com.qubling.sidekick.instance.Gravatar;
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
	
    private static final int TIMEOUT_CONNECTION = 2000;
    private static final int TIMEOUT_SOCKET = 3000;
    
//    private static final String GRAVATAR_TOOK_TOO_LONG = "Gravatar took too long";
//    private static int requestCounter = 0;
    
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

        // Calculate the pixel size of the Gravatar
        Context context = getContext();
        int gravatarPixelSize = Math.min(
                (int) (gravatarDpSize * context.getResources().getDisplayMetrics().density + 0.5f),
                512);
			
		ResultSet<Gravatar> inputResults = getResultSet();
		
		for (Gravatar gravatar : inputResults) {
			try {
				Bitmap bitmap = fetchBitmap(gravatar.getUrl(gravatarPixelSize));
	            gravatar.setBitmap(bitmap);
//	            Log.d("GravatarFetcher", "Fetched " + gravatar.getUrl(gravatarPixelSize));
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

    private Bitmap fetchBitmap(final String gravatarURL) {
        
//        try {
            Timer timer = new Timer();
            try {
    
            	// Make sure we don't get stuck waiting for a Gravatar
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_CONNECTION);
                HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_SOCKET);
    
                // Prepare the request
    //            Log.d("AuthorByDistributionSearch", "Gravatar: " + resizedGravatarURL);
                HttpClient httpClient = getHttpClient();
                final HttpGet req = new HttpGet(gravatarURL);
                req.setParams(httpParams);
                
                // Start the absolute timer for the request
    //            final int reqId = ++requestCounter;
    //            final Date ts = new Date();
                timer.schedule(new TimerTask() {
    				@Override
    				public void run() {
    //					long elapsed = new Date().getTime() - ts.getTime();
    //					Log.w("GravatarFetcher", "Gravatar request #" + reqId + " took too long (" + elapsed + " µs), aborting fetch: " + resizedGravatarURL);
    					Log.w("GravatarFetcher", "Gravatar request took too long, aborting fetch: " + gravatarURL);
    					req.abort();
    					//throw new RuntimeException(GRAVATAR_TOOK_TOO_LONG);
    				}
    			}, timeoutAbsolute);
                
                // Do the request
                HttpResponse res = httpClient.execute(req);
                
                // Cancel as soon as we have the response to avoid weird exceptions
                timer.cancel();
                
                // Get the response content
                HttpEntity entity = res.getEntity();
                InputStream content = entity.getContent();
                Bitmap gravatarBitmap = BitmapFactory.decodeStream(content);
                return gravatarBitmap;
            }
            
            catch (InterruptedIOException e) {
            	// No message, since this probably means Gravatar took too long
            	return null;
            }
    
            catch (IOException e) {
                Log.e("GravatarFetcher", "Error loading Gravatar: " + e, e);
                return null;
            }
    
            // During testing, I sometimes get an IllegalStateException: Connection is not open.
            // This is a stupid exception and nearly any illegal state exception we can ignore by
            // just not loading the Gravatar. The bitmap is not *that* important.
            catch (IllegalStateException e) {
            	Log.e("GravatarFetcher", "Error fetching Gravatar: " + e, e);
            	return null;
            }
            
//            catch (RuntimeException e) {
//            	if (GRAVATAR_TOOK_TOO_LONG.equals(e.getMessage())) {
//            		return null;
//            	}
//            	else {
//            		throw e;
//            	}
//            }
            
            finally {
                // Request is finished, stop the timer
    //          long elapsed = new Date().getTime() - ts.getTime();
    //          Log.d("GravatarFetcher", "Gravatar request #" + reqId + " finished executing request (" + elapsed + " µs)");
              timer.cancel();
            }
//        }
        
        // Guarantee that Gravatar took too long never escapes from here...
//        catch (RuntimeException e) {
//            if (GRAVATAR_TOOK_TOO_LONG.equals(e.getMessage())) {
//                return null;
//            }
//            else {
//                throw e;
//            }
//        }
    }
    
    public String toString() {
    	return getModel() + ":GravatarFetch(" + gravatarDpSize + ";" + getResultSet() + ")";
    }

	@Override
	public SerialUpdateFetcher<Gravatar> thenDoFetch(UpdateFetcher<Gravatar> fetcher) {
		return super.thenDoFetch(fetcher);
	}
}
