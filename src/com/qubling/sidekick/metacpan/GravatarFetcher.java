/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.metacpan;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.qubling.sidekick.metacpan.collection.AuthorList;
import com.qubling.sidekick.metacpan.result.Author;

/**
 * Used to fetch Gravatar bitmaps for a list of authors.
 * 
 * @author sterling
 *
 */
public class GravatarFetcher extends RemoteAPI<Author, Void, Void> {

    private static final float GRAVATAR_DP_SIZE = 35f;
    private static final Pattern RESIZE_GRAVATAR_PATTERN = Pattern.compile("([?&])s=[0-9]+\\b");

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

            // Do the request
            Log.d("AuthorByDistributionSearch", "Gravatar: " + resizedGravatarURL);
            HttpGet req = new HttpGet(resizedGravatarURL);
            HttpResponse res = getClient().execute(req);

            // Get the response content
            HttpEntity entity = res.getEntity();
            InputStream content = entity.getContent();
            Bitmap gravatarBitmap = BitmapFactory.decodeStream(content);
            return gravatarBitmap;
        }

        catch (IOException e) {
            // TODO Return a generic image when this happens
            Log.e("AuthorByDistributionSearch", "Error loading Gravatar: " + e);
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
        super.onPostExecute(result);

        authorList.notifyModelListUpdated();
    }
}
