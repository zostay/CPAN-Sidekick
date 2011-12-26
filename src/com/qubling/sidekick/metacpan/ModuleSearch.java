/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.metacpan;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.qubling.sidekick.metacpan.collection.AuthorList;
import com.qubling.sidekick.metacpan.collection.DistributionList;
import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Author;
import com.qubling.sidekick.metacpan.result.Distribution;
import com.qubling.sidekick.metacpan.result.Module;

public class ModuleSearch extends MetaCPANSearch<Module[]> {

    private String query;

    private ModuleList moduleList;
    private int totalCount = 0;

    public ModuleSearch(Context context, ModuleList list, String query) {
        super(new HttpClientManager(4), context, SearchSection.FILE, "module_search");

        this.query          = query.replace("\"", "\\\"");
        this.moduleList = list;
    }

    @Override
    public JSONObject search() {

        // Setup the query
        String cleanQuery = query.replace("::", " ");

        Map<String, Object> variables = buildVariables(
            "query",      query,
            "cleanQuery", cleanQuery
        );

        return makeMetaCPANRequest(variables);
    }

    @Override
    public Module[] constructCompiledResult(JSONObject searchResult) throws JSONException {

        AuthorList authors = moduleList.extractAuthorList();
        DistributionList distributions = moduleList.extractDistributionList();

        // Slurp up the matches
        JSONArray hits = searchResult.getJSONObject("hits").getJSONArray("hits");
        int modulesCount = hits.length();
        Module[] modules = new Module[modulesCount];
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");

            modules[i] = Module.fromModuleSearch(hit, authors, distributions);
        }

        totalCount = searchResult.getJSONObject("hits").getInt("total");

        return modules;
    }

    @Override
    protected void onPostExecute(Module[] modules) {
        super.onPostExecute(modules);

        // Nothing useful returned, forget it
        if (modules == null) return;

        // Stuff the matches into an adapter and fill the list
        Collections.addAll(moduleList, modules);
        moduleList.setTotalCount(totalCount);
        moduleList.notifyModelListUpdated();

        // Build a distributionMap, and authorMap, and eliminate dupes
        Map<String, Author> authorMap = new HashMap<String, Author>();
        Map<String, Distribution> distributionMap = new HashMap<String, Distribution>();
        for (Module module : moduleList) {

            // map distributions and eliminate dupes
            Distribution distribution = module.getDistribution();
            if (distributionMap.containsKey(distribution.getName())) {
                module.setDistribution(distributionMap.get(distribution.getName()));
            }
            else {
                distributionMap.put(distribution.getName(), distribution);
            }

            // map authors and eliminate dupes
            Author author = module.getAuthor();
            if (authorMap.containsKey(author.getPauseId())) {
                module.setAuthor(authorMap.get(author.getPauseId()));
            }
            else {
                authorMap.put(author.getPauseId(), author);
            }
        }

        // Now, fire off the tasks that fill in the details
        DistributionList distributionList = moduleList.extractDistributionList();
        new AuthorByDistributionSearch(getClientManager(), getContext(), moduleList.extractAuthorList()).execute();
        new FavoriteByDistributionSearch(getClientManager(), getContext(), distributionList, distributionMap).execute();
        new RatingByDistributionSearch(getClientManager(), getContext(), distributionList, distributionMap).execute();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        // Since we start these actions in the post execute, which won't be running,
        // mark them as completed
        getClientManager().markActionCompleted(3);
    }

}
