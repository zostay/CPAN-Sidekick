package com.qubling.sidekick.fetch.cpan;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.instance.Release;
import com.qubling.sidekick.model.Model;
import com.qubling.sidekick.search.ResultSet;

public class ModuleForReleaseFetcher extends CPANQueryFetcher<Module> {
    private Release release;
    
    public ModuleForReleaseFetcher(Model<Module> model, Release release) {
        super(model, SearchSection.FILE, "module_for_release");
        
        this.release = release;
    }

    @Override
    protected boolean shouldCompleteRequest() {
       return true;
    }

    @Override
    protected void prepareRequest(Map<String, Object> variables) {
        variables.put("release_version", release.getName() + "-" + release.getVersion());
        variables.put("author_pauseid", release.getAuthorPauseId());
    }

    @Override
    protected void consumeResponse(JSONObject response) throws JSONException {
        ResultSet<Module> results = getResultSet();
        
        if (response == null) {
            Log.e("ModuleForReleaseFetcher", "Unexpected response (response is null)");
            return;
        }
        
        JSONObject topHits = response.getJSONObject("hits");
        if (topHits == null) {
            Log.e("ModuleKeywordSearch", "Unexpected response (top hits missing): " + response);
            return;
        }
        
        JSONArray hits = topHits.getJSONArray("hits");
        if (hits == null) {
            Log.e("ModuleKeywordSearch", "Unexpected response (nested hits missing): " + response);
            return;
        }

        // Slurp up the matches
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i).getJSONObject("fields");
            
            String name = null;
            try {
                if (!hit.isNull("_source.module")) {
                    name = hit.getJSONArray("_source.module").getJSONObject(0).getString("name");
                }
                else if (!hit.isNull("documentation")) {
                    name = hit.getString("documentation");
                }
                else if (!hit.isNull("path")) {
                    name = hit.getString("path");
                }
            }
            catch (JSONException e) {
                name = "Unknown Module Name";
            }

            String moduleAbstract = null;

            try { if (!hit.isNull("_source.abstract")) moduleAbstract = hit.getString("_source.abstract"); } catch (JSONException e) {}
            
//            Log.d("ModuleForReleaseFetcher", "name: " + name);
//            Log.d("ModuleForReleaseFetcher", "abstract: " + moduleAbstract);
//            Log.d("ModuleForReleaseFetcher", "hit: " + hit.toString());
            
            Module module = getModel().acquireInstance(name);
            if (moduleAbstract != null) module.setModuleAbstract(moduleAbstract);
            module.setRelease(release);
            
            results.add(module);
        }
    }
    
    @Override
    public String toString() {
        return getModel() + ":ModuleForReleaseFetcher(" 
                + release.getAuthorPauseId() + "/" 
                + release.getName() + "-"
                + release.getVersion() + ")";
    }

}
