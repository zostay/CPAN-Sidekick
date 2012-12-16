/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.widget;

import java.util.Collections;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.qubling.sidekick.R;
import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.search.ResultSet;
import com.qubling.sidekick.search.Search;

/**
 * This is a {@link BaseAdapter} for displaying module information in a list
 * view.
 *
 * @author sterling
 *
 */
public class ModuleListAdapter extends BaseAdapter {

    private int VIEW_TYPE_MODULE    = 0;
    private int VIEW_TYPE_LOAD_MORE = 1;
    private int VIEW_TYPE_COUNT     = 2;

    private Search<Module> search;
    
    private LayoutInflater inflater;
    private int moduleLayout;
    private int currentModule = -1;

    private View loadMoreItemsRow;
    
    public ModuleListAdapter(Context context, Search<Module> search, int moduleLayout) {
        this.inflater      = LayoutInflater.from(context);
        this.search        = search;
        this.moduleLayout  = moduleLayout;
    }

    public ModuleListAdapter(Context context, Search<Module> search) {
        this(context, search, R.layout.module_list_item);
    }
    
    private ResultSet<Module> getResultSet() {
    	return search != null ? search.getResultSet() : null;
    }

    private boolean hasMoreItems() {
    	ResultSet<Module> results = getResultSet();
        return results.getTotalSize() > results.size();
    }

    @Override
    public int getCount() {
    	if (getResultSet() == null)
    		return 0;
    	
        return getResultSet().size() + (hasMoreItems() ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (hasMoreItems() && position == getResultSet().size()) {
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
    
    private void requestMoreItems() {
    	search.fetchMore();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	ResultSet<Module> results = getResultSet();

        // Return the load more items row
        if (hasMoreItems() && position == results.size()) {
            requestMoreItems();
            return getLoadMoreItemsRow(parent);
        }

        // How does this happen?
        if (position == results.size()) {
            Log.e("ModuleList", "the load items position was requested but shouldn't have been");
            return null;
        }

        // Otherwise, start working on a regular row

        // Try to convert, if we can
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(moduleLayout, null);
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
    	ResultSet<Module> results = getResultSet();
    	
        if (position < results.size())
            return results.get(position);

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
    
    public int setCurrentModule(Module module) {
        int currentModule = getResultSet().indexOf(module);
        this.currentModule = currentModule;
        notifyDataSetChanged();
        return currentModule;
    }
    
    public void setSearch(Search<Module> search) {
    	this.search = search;
    	notifyDataSetChanged();
    }

    public int getCurrentModule() {
    	return currentModule;
    }
}
