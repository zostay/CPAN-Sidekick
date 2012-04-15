package com.qubling.sidekick.api.cpan;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import com.qubling.sidekick.api.HttpClientManager;
import com.qubling.sidekick.cpan.collection.DistributionList;
import com.qubling.sidekick.cpan.result.Distribution;

public class DistributionFetcher extends MetaCPANAPI<Void, Void, Void> {

    private DistributionList distributionList;

    public DistributionFetcher(HttpClientManager clientManager, DistributionList distributionList) {
        super(clientManager);

        this.distributionList = distributionList;
    }

	@Override
	protected Void doInBackground(Void... params) {
		Distribution distribution = distributionList.get(0);

        String distributionContent;
        try {
            HttpGet distributionReq = new HttpGet(METACPAN_API_RELEASE_URL + distribution.getName());
            HttpResponse distributionRes = getClient().execute(distributionReq);

            distributionContent = slurpContent(distributionRes);
            Object parsedContent = new JSONTokener(distributionContent).nextValue();
            if (parsedContent instanceof JSONObject) {
                JSONObject json = (JSONObject) parsedContent;
                
                // Parse the last updated date
                Date updatedDate;
                try {
                    DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                    String updatedDateStr = json.getString("date");
                    updatedDate = iso8601.parse(updatedDateStr);
                }
                catch (ParseException e) {
                	Log.e("DistributionFetcher", "Cannot parse date for " + distribution.getName() + ": setting to Date(0)");
                	updatedDate = new Date(0);
                }
                	
                // Release Metadata
                distribution.setUpdated(updatedDate);
                distribution.setStatus(json.getString("status"));
                distribution.setMaturity(json.getString("maturity"));
                distribution.setAuthorized(json.getBoolean("authorized"));
                distribution.setLicense(json.getString("license"));
            }
            else {
                // TODO Show an alert dialog or toast when this happens
                Log.e("DistributionFetcher", "Unexpected JSON content: " + parsedContent);
                return null;
            }
        }

        catch (IOException e) {
            Log.e("DistributionFetcher", "Cannot fetch release info for " + distribution.getName() + ": " + e);
            return null;
        }

        catch (JSONException e) {
            Log.e("DistributionFetcher", "Cannot parse release info for " + distribution.getName() + ": " + e);
            return null;
        }

        return null;
	}

    @Override
    protected void onPostExecute(Void result) {
        distributionList.notifyModelListUpdated();

        super.onPostExecute(result);
    }

}
