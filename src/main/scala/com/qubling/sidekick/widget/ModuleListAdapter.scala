/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.widget

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import com.qubling.sidekick.R
import com.qubling.sidekick.instance.Module
import com.qubling.sidekick.search.ResultSet
import com.qubling.sidekick.search.Search

/**
 * This is a {@link BaseAdapter} for displaying module information in a list
 * view.
 *
 * @author sterling
 *
 */
class ModuleListAdapter(
  context : Context, 
  private var search : Search[Module], 
  private val moduleLayout : Int
) extends BaseAdapter {

  private val ViewTypeModule   = 0
  private val ViewTypeLoadMore = 1
  private val ViewTypeCount    = 2

  private val inflater : LayoutInflater = LayoutInflater.from(context)
  private var _currentModule = -1

  private def loadMoreItemsRow(parent : ViewGroup) : View =
    inflater.inflate(R.layout.module_list_load_more, parent, false)
    
  def this(context : Context, search : Search[Module]) =
    this(context, search, R.layout.module_list_item)
    
  private def resultSet : Option[ResultSet[Module]] = 
    if (search != null) Some(search.getResultSet) else None

  private def hasMoreItems : Boolean =
    resultSet match {
      case Some(results) => results.getTotalSize > results.size
      case None => false
    }

  override def getCount : Int =
    resultSet match {
      case Some(results) => results.size + (if (hasMoreItems) 1 else 0)
      case None => 0
    }

  override def getItemViewType(position : Int) : Int =
    resultSet match {
      case Some(results) => 
        if (hasMoreItems && position == results.size) 
          ViewTypeLoadMore
        else
          ViewTypeModule
      case None => -1
    }

  override def getViewTypeCount : Int = ViewTypeCount
    
  private def requestMoreItems() : Unit = search.fetchMore()

  override def getView(position : Int, convertView : View, parent : ViewGroup) : View = {
    resultSet match {
      case Some(results) =>

        // Return the load more items row
        if (hasMoreItems && position == results.size) {
            requestMoreItems()
            loadMoreItemsRow(parent)
        }
        
        // How does this happen?
        else if (position == results.size) {
            Log.e("ModuleList", "the load items position was requested but shouldn't have been")
            null
        }

        // Otherwise, start working on a regular row
        else {

          // Try to convert, if we can
          var row = convertView;
          if (row == null) {
            row = inflater.inflate(moduleLayout, null)
          }

          if (position == currentModule) {
        	row.setBackgroundResource(R.drawable.listitem_background_picked)
          }
          else {
        	row.setBackgroundResource(android.R.color.transparent)
          }

          // Get the module for this position
          var item = getItem(position)

          // Update it using the helper procedure
          ModuleHelper.updateItem(row, item)

          // Return the module item view
          row
        }
      case None => null
    }
  }

  override def getItem(position : Int) : Module = 
    resultSet match {
      case Some(results) =>
        if (position < results.size)
          results.get(position)
        else
          null
      case None => null
    }

  override def getItemId(position : Int) : Long = position
  override def hasStableIds : Boolean = false

  def currentModule_=(module : Int) {
    _currentModule = module
    notifyDataSetChanged()
  }

  def currentModule_=(module : Module) {
    resultSet match {
      case Some(results) => 
        currentModule = results.indexOf(module)
      case None => ()
    }
  }

  def currentModule : Int = _currentModule
    
  def setSearch(newSearch : Search[Module]) {
    search = newSearch
    notifyDataSetChanged()
  }
}
