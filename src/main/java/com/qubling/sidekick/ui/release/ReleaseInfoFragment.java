package com.qubling.sidekick.ui.release;

import java.util.Comparator;

import com.qubling.sidekick.R;
import com.qubling.sidekick.fetch.Fetcher;
import com.qubling.sidekick.instance.Gravatar;
import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.instance.Release;
import com.qubling.sidekick.model.ModuleModel;
import com.qubling.sidekick.model.ReleaseModel;
import com.qubling.sidekick.search.ResultSet;
import com.qubling.sidekick.search.Schema;
import com.qubling.sidekick.search.Search;
import com.qubling.sidekick.ui.module.ModuleFragment;
import com.qubling.sidekick.widget.ModuleListAdapter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ReleaseInfoFragment extends ModuleFragment {

    private Schema searchSession;
    private Release release;
    private Module module;
    
    private Search<Module> moduleSearch;
    
    public void setRelease(Release release) {
        this.release = release;
        
        updateReleaseInfo();
    }
    
    public Release getRelease() {
        return release;
    }
    
    private void updateReleaseInfo() {
        TextView releaseName = (TextView) getActivity().findViewById(R.id.release_name);
        StringBuilder distVersion = new StringBuilder();
        distVersion.append(release.getName());
        distVersion.append('-');
        distVersion.append(release.getVersion());
        releaseName.setText(distVersion);
        
        TextView authorFullName = (TextView) getActivity().findViewById(R.id.module_author_fullname);
        authorFullName.setText(release.getAuthor().getFullName());
        
        TextView releaseMeta = (TextView) getActivity().findViewById(R.id.release_metadata);
        releaseMeta.setText("License: " + (release.getLicense() == null ? "..." : release.getLicense()));
        
        TextView authorPauseId = (TextView) getActivity().findViewById(R.id.module_author_pauseid);
        authorPauseId.setText(release.getAuthorPauseId());

        // Set the rating bar
        RatingBar distRating = (RatingBar) getActivity().findViewById(R.id.module_release_rating);
        distRating.setRating((float) release.getRatingMean());

        // Set the rating count
        TextView distRatingCount = (TextView) getActivity().findViewById(R.id.module_release_rating_count);
        distRatingCount.setText(String.valueOf(release.getRatingCount()));

        // Set the favorite count
        Button favoriteCount = (Button) getActivity().findViewById(R.id.module_release_favorite);
        if (release.getFavoriteCount() > 0) {
            favoriteCount.setText(release.getFavoriteCount() + "++ ");
            favoriteCount.setBackgroundResource(R.drawable.btn_favorite_others);
            favoriteCount.setShadowLayer(1.5f, 1f, 1f, R.color.favorite_text_shadow_color);
        }

        // Not favorited yet, set it to a blank
        else {
            favoriteCount.setText("++ ");
            favoriteCount.setBackgroundResource(R.drawable.btn_favorite_default);
            favoriteCount.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        }

        // Mark this as our favorite
        if (release.isMyFavorite()) {
            favoriteCount.setBackgroundResource(R.drawable.btn_favorite_mine);
        }

        // Set the quick contact badge to the author's picture
        Gravatar gravatar = release.getAuthor().getGravatar();
        QuickContactBadge badge = (QuickContactBadge) getActivity().findViewById(R.id.module_author_avatar);
        if (gravatar != null && gravatar.getBitmap() != null) {
            badge.setImageBitmap(gravatar.getBitmap());
        }

        // No user picture, set to default
        else {
            badge.setImageResource(R.drawable.ic_contact_picture);
        }   
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        searchSession = new Schema(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.release_view_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        fetchRelease();
    }
    
    private void fetchRelease() {
        fetchReleaseMetadata();
        fetchReleaseModules();
    }
    
    private void fetchReleaseMetadata() {
        ReleaseModel releases = searchSession.getReleaseModel();
        Fetcher<Release> releaseFetch = releases.fetch();
        releaseFetch.getResultSet().add(release);
        
        Search<Release> releaseMetaSearch = searchSession.doFetch(releaseFetch, new Fetcher.OnFinished<Release>() {
            @Override
            public void onFinishedFetch(Fetcher<Release> fetcher, ResultSet<Release> results) {
                // Don't do anything if we don't have an activity (i.e., don't NPE either)
                if (getActivity() == null) return;        
                updateReleaseInfo();
            }
        });
        releaseMetaSearch.start();
    }
    
    private void fetchReleaseModules() {
        ModuleModel modules = searchSession.getModuleModel();
        Fetcher<Module> modulesFetch = modules.fetchModulesForRelease(release);
        
        moduleSearch = searchSession.doFetch(modulesFetch, new Fetcher.OnFinished<Module>() {
            @Override
            public void onFinishedFetch(Fetcher<Module> fetcher, ResultSet<Module> results) {
                results.sort(new Comparator<Module>() {
                    @Override
                    public int compare(Module a, Module b) {
                        return a.getName().compareToIgnoreCase(b.getName());
                    }
                });
                
                if (getActivity() == null) return;
                
                ModuleListAdapter adapter = new ModuleListAdapter(getActivity(), moduleSearch, R.layout.module_list_item_simplified);
                ListView moduleListView = (ListView) getActivity().findViewById(R.id.release_modules_list);
                moduleListView.setAdapter(adapter);
                if (module != null) {
                    adapter.currentModule_$eq(module);
                    int position = adapter.currentModule();
                    moduleListView.setSelection(position);
                }

                moduleListView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View row, int position, long id) {
                        ListView moduleListView = (ListView) parent;
                        Module currentModule = (Module) moduleListView.getItemAtPosition(position);

                        // This happens when you click on the progress throbber item
                        if (currentModule == null)
                            return;

                        getModuleActivity().onModuleClick(currentModule);

                        ModuleListAdapter adapter = (ModuleListAdapter) parent.getAdapter();
                        adapter.currentModule_$eq(position);
                        module = adapter.getItem(position);
                    }
                });
            }
        });
        
        moduleSearch.start();
    }
    
    public void selectModule(Module module) {
        this.module = module;
        
        ListView moduleListView = (ListView) getActivity().findViewById(R.id.release_modules_list);
        if (moduleListView == null) return;
        
        ModuleListAdapter adapter = (ModuleListAdapter) moduleListView.getAdapter();
        if (adapter == null) return;
        
        adapter.currentModule_$eq(module);
    }
}
