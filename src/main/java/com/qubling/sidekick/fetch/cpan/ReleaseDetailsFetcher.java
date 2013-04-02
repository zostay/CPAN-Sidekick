package com.qubling.sidekick.fetch.cpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import com.qubling.sidekick.instance.Release;
import com.qubling.sidekick.model.Model;

public class ReleaseDetailsFetcher extends CPANDirectFetcher<Release> {

    public ReleaseDetailsFetcher(Model<Release> model) {
        super(model, FetchSection.RELEASE_FETCH);
    }

    @Override
    public boolean needsUpdate(Release release) {
        return release.getLicense() == null;
    }

    @Override
    public void consumeResponse(String content, Release release) {
        try {
            Object parsedContent = new JSONTokener(content).nextValue();
            if (parsedContent instanceof JSONObject) {
                JSONObject json = (JSONObject) parsedContent;

                // Release Metadata
                JSONArray licenses = json.getJSONArray("license");
                StringBuilder licensesStr = new StringBuilder();
                boolean needComma = false;
                for (int i = 0; i < licenses.length(); i++) {
                    if (needComma) licensesStr.append(", ");
                    licensesStr.append(licenses.getString(0));
                    needComma = true;
                }
                release.setLicense(licensesStr.toString());
            }
            else {
                // TODO Show an alert dialog or toast when this happens
                Log.e("ReleaseDetailsFetcher", "Unexpected JSON content: " + parsedContent);
            }
        }
        catch (JSONException e) {
            // TODO Show an alert dialog or toast when this happens
            Log.e("ReleaseDetailsFetcher", "Error reading JSON response while fetching details: " + e.getMessage(), e);
        }
    }

}
