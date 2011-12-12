package com.qubling.sidekick.widget;

import com.qubling.sidekick.R;
import com.qubling.sidekick.metacpan.collection.ModuleList;
import com.qubling.sidekick.metacpan.result.Module;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

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
		
		// Update it using the helper procedure
		ModuleHelper.updateItem(row, item);
		
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
