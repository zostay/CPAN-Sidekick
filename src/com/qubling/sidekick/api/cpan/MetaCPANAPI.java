/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.api.cpan;

import com.qubling.sidekick.api.HttpClientManager;
import com.qubling.sidekick.api.RemoteAPI;

import android.os.AsyncTask;

/**
 * A subclass of {@link RemoteAPI} with some tools for accessing MetaCPAN.
 * 
 * @author sterling
 *
 * @param <Params> See {@link AsyncTask}
 * @param <Progress> See {@link AsyncTask}
 * @param <Result> See {@link AsyncTask}
 */
public abstract class MetaCPANAPI<Params, Progress, Result> extends RemoteAPI<Params, Progress, Result> {

    public static final String METACPAN_API_URL = "http://api.metacpan.org/";
    public static final String METACPAN_API_POD_URL = METACPAN_API_URL + "pod/";
    public static final String METACPAN_API_MODULE_URL = METACPAN_API_URL + "module/";

    public static final String METACPAN_URL = "http://metacpan.org/";
    public static final String METACPAN_MODULE_URL = METACPAN_URL + "module/";

    public MetaCPANAPI(HttpClientManager clientManager) {
        super(clientManager);
    }
}
