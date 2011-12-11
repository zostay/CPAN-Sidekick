package com.qubling.sidekick.widget;

import com.qubling.sidekick.R;
import com.qubling.sidekick.metacpan.result.Module;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.RatingBar;
import android.widget.TextView;

public final class ModuleHelper {
	private ModuleHelper() {
	}
	
	public static void updateItem(View row, Module item) {
		
		// Set the module name and abstract
		TextView moduleName = (TextView) row.findViewById(R.id.module_name);
		SpannableStringBuilder formattedString = new SpannableStringBuilder(item.getModuleName());
		if (item.getModuleAbstract() != null) {
			formattedString.append(" - " + item.getModuleAbstract());
			
			ForegroundColorSpan color = new ForegroundColorSpan(Color.GRAY);
			formattedString.setSpan(
					color, 
					formattedString.length() - item.getModuleAbstract().length() - 3, 
					formattedString.length(), 
					0);
		}
		moduleName.setText((CharSequence) formattedString);
		
		// Set the distribution author, name, and version
		TextView distributionName = (TextView) row.findViewById(R.id.module_author_distribution);
		distributionName.setText(
				item.getAuthorPauseId()
				+ "/" + item.getDistributionName()
				+ "-" + item.getDistributionVersion());
		
		// Set the rating bar
		RatingBar distRating = (RatingBar) row.findViewById(R.id.module_release_rating);
		distRating.setRating((float) item.getDistributionRating());
		
		// Set the rating count
		TextView distRatingCount = (TextView) row.findViewById(R.id.module_release_rating_count);
		distRatingCount.setText(String.valueOf(item.getDistributionRatingCount()));
		
		// Set the favorite count
		Button favoriteCount = (Button) row.findViewById(R.id.module_release_favorite);
		if (item.getDistributionFavoriteCount() > 0) {
			favoriteCount.setText(item.getDistributionFavoriteCount() + "++ ");
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
		if (item.isDistributionMyFavorite()) {
			favoriteCount.setBackgroundResource(R.drawable.btn_favorite_mine);
		}
		
		// Set the quick contact badge to the author's picture
		QuickContactBadge badge = (QuickContactBadge) row.findViewById(R.id.module_author_avatar);
		if (item.getAuthorGravatarBitmap() != null) {
			badge.setImageBitmap(item.getAuthorGravatarBitmap());
		}
		
		// No user picture, set to default
		else {
			badge.setImageResource(R.drawable.ic_contact_picture);
		}
	}
}
