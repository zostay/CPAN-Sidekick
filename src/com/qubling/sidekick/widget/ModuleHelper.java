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
import com.qubling.sidekick.metacpan.result.Module;

public final class ModuleHelper {
    private ModuleHelper() { }

    public static void updateItem(View row, Module item) {

        // Set the module name and abstract
        TextView moduleName = (TextView) row.findViewById(R.id.module_name);
        SpannableStringBuilder formattedString = new SpannableStringBuilder(item.getName());
        if (item.getAbstract() != null) {
            formattedString.append(" - " + item.getAbstract());

            ForegroundColorSpan color = new ForegroundColorSpan(Color.GRAY);
            formattedString.setSpan(
                    color,
                    formattedString.length() - item.getAbstract().length() - 3,
                    formattedString.length(),
                    0);
        }
        moduleName.setText(formattedString);

        // Set the distribution author, name, and version
        TextView distributionName = (TextView) row.findViewById(R.id.module_author_distribution);
        distributionName.setText(
                item.getAuthor().getPauseId()
                + "/" + item.getDistribution().getName()
                + "-" + item.getDistribution().getVersion());

        // Set the rating bar
        RatingBar distRating = (RatingBar) row.findViewById(R.id.module_release_rating);
        distRating.setRating((float) item.getDistribution().getRating());

        // Set the rating count
        TextView distRatingCount = (TextView) row.findViewById(R.id.module_release_rating_count);
        distRatingCount.setText(String.valueOf(item.getDistribution().getRatingCount()));

        // Set the favorite count
        Button favoriteCount = (Button) row.findViewById(R.id.module_release_favorite);
        if (item.getDistribution().getFavoriteCount() > 0) {
            favoriteCount.setText(item.getDistribution().getFavoriteCount() + "++ ");
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
        if (item.getDistribution().isMyFavorite()) {
            favoriteCount.setBackgroundResource(R.drawable.btn_favorite_mine);
        }

        // Set the quick contact badge to the author's picture
        QuickContactBadge badge = (QuickContactBadge) row.findViewById(R.id.module_author_avatar);
        if (item.getAuthor().getGravatarBitmap() != null) {
            badge.setImageBitmap(item.getAuthor().getGravatarBitmap());
        }

        // No user picture, set to default
        else {
            badge.setImageResource(R.drawable.ic_contact_picture);
        }
    }
}
