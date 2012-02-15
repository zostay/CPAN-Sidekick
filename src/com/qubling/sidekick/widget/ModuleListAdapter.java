/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.widget;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.qubling.sidekick.R;
import com.qubling.sidekick.cpan.collection.ModelList;
import com.qubling.sidekick.cpan.collection.ModuleList;
import com.qubling.sidekick.cpan.result.Module;

/**
 * This is a {@link BaseAdapter} for displaying module infomration in a list
 * view.
 * 
 * @author sterling
 *
 */
public class ModuleListAdapter extends BaseAdapter implements ModuleList.OnModuleListUpdated {

    private int VIEW_TYPE_MODULE    = 0;
    private int VIEW_TYPE_LOAD_MORE = 1;
    private int VIEW_TYPE_COUNT     = 2;

    private ModuleList moduleList;
    private LayoutInflater inflater;
    private int currentModule = -1;

    private View loadMoreItemsRow;

    public ModuleListAdapter(Context context, ModuleList items) {
        this.inflater    = LayoutInflater.from(context);
        this.moduleList  = items;

        moduleList.addModelListUpdatedListener(this);
    }

    private boolean hasMoreItems() {
        return moduleList.getTotalCount() > moduleList.size();
    }

    @Override
    public void onModelListUpdated(ModelList<Module> list) {
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
        
        if (position == currentModule) {
        	row.setBackgroundResource(R.drawable.listitem_background_picked);
        }
        else {
        	row.setBackgroundResource(android.R.color.transparent);
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
    
    public void setCurrentModule(int currentModule) {
    	this.currentModule = currentModule;
    	notifyDataSetChanged();
    }
    
    public int getCurrentModule() {
    	return currentModule;
    }
}
