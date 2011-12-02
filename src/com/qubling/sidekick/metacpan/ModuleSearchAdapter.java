package com.qubling.sidekick.metacpan;

import com.qubling.sidekick.R;
import com.qubling.sidekick.metacpan.result.Module;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.RatingBar;
import android.widget.TextView;

public class ModuleSearchAdapter extends ArrayAdapter<Module> {
	
	private int layout;
	private LayoutInflater inflater;

	public ModuleSearchAdapter(Context context, int layout, Module[] items) {
		super(context, layout, items);
		
		this.layout = layout;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			row = inflater.inflate(layout, null);
		}
		
		Module item = getItem(position);
		
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
		
		TextView distributionName = (TextView) row.findViewById(R.id.module_author_distribution);
		distributionName.setText(
				item.getAuthorPauseId()
				+ "/" + item.getDistributionName()
				+ "-" + item.getDistributionVersion());
		
		RatingBar distRating = (RatingBar) row.findViewById(R.id.module_release_rating);
		distRating.setRating((float) item.getDistributionRating());
		
		TextView distRatingCount = (TextView) row.findViewById(R.id.module_release_rating_count);
		distRatingCount.setText(String.valueOf(item.getDistributionRatingCount()));
		
		QuickContactBadge badge = (QuickContactBadge) row.findViewById(R.id.module_author_avatar);
		if (item.getAuthorGravatarBitmap() != null) {
			badge.setImageBitmap(item.getAuthorGravatarBitmap());
		}
		else {
			badge.setImageResource(R.drawable.ic_contact_picture);
		}
		
		return row;
	}
	
}
