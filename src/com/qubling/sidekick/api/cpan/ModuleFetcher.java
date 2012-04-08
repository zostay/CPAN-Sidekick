/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.api.cpan;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import com.qubling.sidekick.api.HttpClientManager;
import com.qubling.sidekick.cpan.collection.ModuleList;
import com.qubling.sidekick.cpan.result.Author;
import com.qubling.sidekick.cpan.result.Distribution;
import com.qubling.sidekick.cpan.result.Module;

/**
 * A tool for fetching the details regarding a single module.
 *
 * @author sterling
 *
 */
public class ModuleFetcher extends MetaCPANAPI<Void, Void, Void> {

    private ModuleList moduleList;

    public ModuleFetcher(HttpClientManager clientManager, ModuleList moduleList) {
        super(clientManager);

        this.moduleList = moduleList;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Module module = moduleList.get(0);

        String moduleContent;
        try {
            HttpGet moduleReq = new HttpGet(METACPAN_API_MODULE_URL + module.getName());
            HttpResponse moduleRes = getClient().execute(moduleReq);

            moduleContent = slurpContent(moduleRes);
            Object parsedContent = new JSONTokener(moduleContent).nextValue();
            if (parsedContent instanceof JSONObject) {
                JSONObject json = (JSONObject) parsedContent;

                // Basic Module info
                module.setAbstract(json.getString("abstract"));

                // Basic Author Info
                Author author = new Author(json.getString("author"));

                // Basic Distribution Info
                module.setDistribution(
                		new Distribution(
                				json.getString("distribution"),
                				json.getString("version"),
                				author));
                
            }
            else {
                // TODO Show an alert dialog or toast when this happens
                Log.e("ModuleFetcher", "Unexpected JSON content: " + parsedContent);
                return null;
            }
        }

        catch (IOException e) {
            Log.e("ModulePODFetcher", "Cannot fetch module info for " + module.getName() + ": " + e);
            return null;
        }

        catch (JSONException e) {
            Log.e("ModulePODFetcher", "Cannot parse module info for " + module.getName() + ": " + e);
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        moduleList.notifyModelListUpdated();

        super.onPostExecute(result);
    }

}
