/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.widget;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.RatingBar;
import android.widget.TextView;

import com.qubling.sidekick.R;
import com.qubling.sidekick.instance.Author;
import com.qubling.sidekick.instance.Gravatar;
import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.instance.Release;

/**
 * Tools for filling in module data into views.
 *
 * @author sterling
 *
 */
public final class ModuleHelper {
    private ModuleHelper() { }

    public static void updateItem(View row, Module item) {

        // Set the module name and abstract
        TextView moduleName = (TextView) row.findViewById(R.id.module_name);
        SpannableStringBuilder formattedString = new SpannableStringBuilder(item.getName());
        if (item.getModuleAbstract() != null) {
            formattedString.append(" - " + item.getModuleAbstract());

            ForegroundColorSpan color = new ForegroundColorSpan(Color.GRAY);
            formattedString.setSpan(
                    color,
                    formattedString.length() - item.getModuleAbstract().length() - 3,
                    formattedString.length(),
                    0);
        }
        moduleName.setText(formattedString);

        Release release = item.getRelease();
        if (release == null) {
        	item.setReleaseName("...");
        	release = item.getRelease();
        }

        Author author = release.getAuthor();
        if (author == null) {
        	release.setAuthorPauseId("...");
        	author = release.getAuthor();
        }
        
        Gravatar gravatar = author.getGravatar();

        // Set the distribution author, name, and version
        TextView releaseName = (TextView) row.findViewById(R.id.module_author_distribution);
        StringBuilder authorDist = new StringBuilder();
        authorDist.append(author.getPauseId());
        authorDist.append('/');
        authorDist.append(release.getName());
        authorDist.append('-');
        authorDist.append(release.getVersion());
        releaseName.setText(authorDist);

        // Set the rating bar
        RatingBar distRating = (RatingBar) row.findViewById(R.id.module_release_rating);
        distRating.setRating((float) release.getRatingMean());

        // Set the rating count
        TextView distRatingCount = (TextView) row.findViewById(R.id.module_release_rating_count);
    	distRatingCount.setText(String.valueOf(release.getRatingCount()));

        // Set the favorite count
        Button favoriteCount = (Button) row.findViewById(R.id.module_release_favorite);
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
        QuickContactBadge badge = (QuickContactBadge) row.findViewById(R.id.module_author_avatar);
        if (gravatar != null && gravatar.getBitmap() != null) {
            badge.setImageBitmap(gravatar.getBitmap());
        }

        // No user picture, set to default
        else {
            badge.setImageResource(R.drawable.ic_contact_picture);
        }
    }
}
