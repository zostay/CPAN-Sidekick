/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.metacpan;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import android.os.AsyncTask;

public abstract class RemoteAPI<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private final HttpClientManager clientManager;

    public RemoteAPI(HttpClientManager clientManager) {
        this.clientManager = clientManager;
    }

    @Override
    protected void onPostExecute(Result result) {
        clientManager.markActionCompleted();
    }

    @Override
    protected void onCancelled() {

        // Even if we don't do anything, we must mark this as completed to avoid a leak
        clientManager.markActionCompleted();
    }

    public HttpClientManager getClientManager() {
        return clientManager;
    }

    public HttpClient getClient() {
        return clientManager.getClient();
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
