package com.qubling.sidekick.widget;

import com.qubling.sidekick.R;
import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.RatingBar;
import android.widget.TextView;

public class ModuleListAdapter extends BaseAdapter implements ModuleList.OnModuleListUpdated {
	
	private int VIEW_TYPE_MODULE    = 0;
	private int VIEW_TYPE_LOAD_MORE = 1;
	private int VIEW_TYPE_COUNT     = 2;
	
	private Context context;
	private ModuleList moduleList;
	private LayoutInflater inflater;
	
	private View loadMoreItemsRow;

	public ModuleListAdapter(Context context, ModuleList items) {
		this.context     = context;
		this.inflater    = LayoutInflater.from(context);
		this.moduleList  = items;
		
		moduleList.addModuleListUpdater(this);
	}
	
	private boolean hasMoreItems() {
		return moduleList.getTotalCount() > moduleList.size();
	}
	
	public void onModuleListUpdate(ModuleList list) {
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return moduleList.size() + (hasMoreItems() ? 1 : 0);
	}

	@Override
	public int getItemViewType(int position) {
		if (hasMoreItems() && position == moduleList.size()) {
			return VIEW_TYPE_LOAD_MORE;
		}
		else {
			return VIEW_TYPE_MODULE;
		}
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_COUNT;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		// Return the load more items row
		if (hasMoreItems() && position == moduleList.size()) {
			moduleList.requestMoreItems();
			return getLoadMoreItemsRow(parent);
		}
		
		// How does this happen?
		if (position == moduleList.size()) {
			Log.e("ModuleList", "the load items position was requested but shouldn't have been");
			return null;
		}
		
		// Otherwise, start working on a regular row
		
		// Try to convert, if we can
		View row = convertView;
		if (row == null) {
			row = inflater.inflate(R.layout.module_list_item, null);
		}
		
		// Get the module for this position
		Module item = getItem(position);
		
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
		
		// Return the module item view
		return row;
	}
	
	private View getLoadMoreItemsRow(ViewGroup parent) {
		
		// Use the one already made, if we've been here
		if (loadMoreItemsRow != null) 
			return loadMoreItemsRow;
		
		// Otherwise make one and cache it
		return loadMoreItemsRow = inflater.inflate(R.layout.module_list_load_more, parent, false);
	}

	@Override
	public Module getItem(int position) {
		if (position < moduleList.size())
			return moduleList.get(position);
		
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public boolean hasStableIds() {
		return false;
	}
}
