package com.qubling.sidekick.ui.release;

import com.qubling.sidekick.R;
import com.qubling.sidekick.instance.Gravatar;
import com.qubling.sidekick.instance.Release;

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

public class ReleaseInfoFragment extends Fragment {
    
    private Release release;
    
    public void setRelease(Release release) {
        this.release = release;
        
        TextView releaseName = (TextView) getActivity().findViewById(R.id.release_name);
        StringBuilder distVersion = new StringBuilder();
        distVersion.append(release.getName());
        distVersion.append('-');
        distVersion.append(release.getVersion());
        releaseName.setText(distVersion);
        
        TextView authorFullName = (TextView) getActivity().findViewById(R.id.module_author_fullname);
        authorFullName.setText("Not Yet Implemented");
        
        TextView releaseMeta = (TextView) getActivity().findViewById(R.id.release_metadata);
        releaseMeta.setText("Not Yet Implemented");
        
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
    
    public Release getRelease() {
        return release;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.release_view_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        
    }

}
