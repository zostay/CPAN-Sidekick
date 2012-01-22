/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.api.cpan;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
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

import com.qubling.sidekick.api.HttpClientManager;
import com.qubling.sidekick.api.RemoteAPI;
import com.qubling.sidekick.cpan.collection.AuthorList;
import com.qubling.sidekick.cpan.result.Author;

/**
 * Used to fetch Gravatar bitmaps for a list of authors.
 * 
 * @author sterling
 *
 */
public class GravatarFetcher extends RemoteAPI<Author, Void, Void> {

    private static final float GRAVATAR_DP_SIZE = 61f;
    private static final Pattern RESIZE_GRAVATAR_PATTERN = Pattern.compile("([?&])s=[0-9]+\\b");
    
    private static final int TIMEOUT_CONNECTION = 2000;
    private static final int TIMEOUT_SOCKET = 3000;
    public static final int TIMEOUT_ABSOLUTE = 10000;

    private Context context;
    private AuthorList authorList;

    public GravatarFetcher(Context context, HttpClientManager clientManager, AuthorList authorList) {
        super(clientManager);

        this.context       = context;
        this.authorList    = authorList;
    }

    private Bitmap fetchBitmap(String gravatarURL) {

        // Calculate the pixel size of the Gravatar
        int gravatarPixelSize = Math.min(
                (int) (GRAVATAR_DP_SIZE * context.getResources().getDisplayMetrics().density + 0.5f),
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
            HttpResponse res = getClient().execute(req);

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
    protected Void doInBackground(Author... authors) {

        Set<String> encounteredAuthors = new HashSet<String>();
        for (Author author : authors) {

            // Don't fetch the same author's gravatar twice, it can only lead to tears
            // (or concurrency problems, whichever comes first)
            if (encounteredAuthors.contains(author.getPauseId())) continue;
            encounteredAuthors.add(author.getPauseId());

            String url = author.getGravatarURL();
            Bitmap gravatar = fetchBitmap(url);
            author.setGravatarBitmap(gravatar);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        authorList.notifyModelListUpdated();
        
        super.onPostExecute(result);
    }
}
