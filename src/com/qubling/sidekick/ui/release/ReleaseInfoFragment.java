package com.qubling.sidekick.ui.release;

import com.qubling.sidekick.R;
import com.qubling.sidekick.fetch.Fetcher;
import com.qubling.sidekick.instance.Gravatar;
import com.qubling.sidekick.instance.Release;
import com.qubling.sidekick.model.ReleaseModel;
import com.qubling.sidekick.search.ResultSet;
import com.qubling.sidekick.search.Schema;
import com.qubling.sidekick.search.Search;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.RatingBar;
import android.widget.TextView;

public class ReleaseInfoFragment extends Fragment implements Fetcher.OnFinished<Release> {

    private Schema searchSession;
    private Release release;
    
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
    
    public void fetchRelease() {
        ReleaseModel releases = searchSession.getReleaseModel();
        Fetcher<Release> releaseFetch = releases.fetch();
        releaseFetch.getResultSet().add(release);
        
        Search<Release> search = searchSession.doFetch(releaseFetch, this);
        
        search.start();
    }

    @Override
    public void onFinishedFetch(Fetcher<Release> fetcher, ResultSet<Release> results) {
        
        // Don't do anything if we don't have an activity (i.e., don't NPE either)
        if (getActivity() == null) return;
        
        updateReleaseInfo();
    }
}
