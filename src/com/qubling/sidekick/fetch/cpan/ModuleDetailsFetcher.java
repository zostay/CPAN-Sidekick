package com.qubling.sidekick.fetch.cpan;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.model.Module;

import android.util.Log;

public class ModuleDetailsFetcher extends CPANDirectFetcher<Module> {

	public ModuleDetailsFetcher(Model<Module> model) {
	    super(model, FetchSection.MODULE_FETCH);
    }

	@Override
	public boolean needsUpdate(Module module) {
		return module.getAbstract() == null
			|| module.getReleaseName() == null
			|| module.getRelease().getVersion() == null
			|| module.getAuthorPauseId() == null;
	}

	@Override
	public void consumeResponse(String content, Module module) {
		try {
            Object parsedContent = new JSONTokener(content).nextValue();
            if (parsedContent instanceof JSONObject) {
                JSONObject json = (JSONObject) parsedContent;

                // Basic Module info
                module.setAbstract(json.getString("abstract"));

                // Basic Distribution Info
                module.setReleaseName(json.getString("distribution"));
                module.getRelease().setVersion(json.getString("version"));

                // Basic Author Info
                module.getRelease().setAuthorPauseId(json.getString("author"));
            }
            else {
                // TODO Show an alert dialog or toast when this happens
                Log.e("ModuleDetailsFetcher", "Unexpected JSON content: " + parsedContent);
            }
		}
		catch (JSONException e) {
			// TODO Show an alert dialog or toast when this happens
			Log.e("ModuleDetailFetcher", "Error reading JSON response while fetching details: " + e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return getModel() + ":ModuleDetailsFetcher(" + getResultSet() + ")";
	}
}
