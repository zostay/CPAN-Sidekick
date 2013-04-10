/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.widget

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.QuickContactBadge
import android.widget.RatingBar
import android.widget.TextView

import com.qubling.sidekick.R
import com.qubling.sidekick.instance.Author
import com.qubling.sidekick.instance.Gravatar
import com.qubling.sidekick.instance.Module
import com.qubling.sidekick.instance.Release

/**
 * Tools for filling in module data into views.
 *
 * @author sterling
 *
 */
object ModuleHelper {
  def updateItem(row : View, item : Module) {

    // Set the module name and abstract
    val moduleName = row.findViewById(R.id.module_name).asInstanceOf[TextView]
    val moduleAbstract = row.findViewById(R.id.module_abstract).asInstanceOf[TextView]
        
    val formattedString = new SpannableStringBuilder(item.getName)
    if (item.getModuleAbstract() != null) {
      if (moduleAbstract != null) {
        moduleAbstract.setText(item.getModuleAbstract())
      }
      else {
        formattedString.append(" - " + item.getModuleAbstract)
    
        val color = new ForegroundColorSpan(Color.GRAY)
        formattedString.setSpan(
          color,
          formattedString.length - item.getModuleAbstract.length - 3,
          formattedString.length,
          0)
      }
    }
        
    moduleName.setText(formattedString)

    var release = item.getRelease match {
      case null => 
        item.setReleaseName("...")
        item.getRelease
      case r => r
    }

    var author = release.getAuthor match {
      case null =>
        release.setAuthorPauseId("...")
        release.getAuthor
      case a => a
    }
        
    val gravatar = author.getGravatar

    // Set the distribution author, name, and version
    val releaseName = row.findViewById(R.id.module_author_distribution).asInstanceOf[TextView]
    if (releaseName != null) {
      val authorDist = new StringBuilder
      authorDist append author.getPauseId
      authorDist append '/'
      authorDist append release.getName
      authorDist append '-'
      authorDist append release.getVersion

      releaseName.setText(authorDist)
    }

    // Set the rating bar
    val distRating = row.findViewById(R.id.module_release_rating).asInstanceOf[RatingBar]
    if (distRating != null) {
      distRating.setRating(release.getRatingMean.toFloat)
    }

    // Set the rating count
    val distRatingCount = row.findViewById(R.id.module_release_rating_count).asInstanceOf[TextView]
    if (distRatingCount != null) {
      distRatingCount.setText(String.valueOf(release.getRatingCount))
    }

    // Set the favorite count
    val favoriteCount = row.findViewById(R.id.module_release_favorite).asInstanceOf[Button]
    if (favoriteCount != null) {
      if (release.getFavoriteCount > 0) {
        favoriteCount.setText(release.getFavoriteCount + "++ ");
        favoriteCount.setBackgroundResource(R.drawable.btn_favorite_others)
        favoriteCount.setShadowLayer(1.5f, 1f, 1f, R.color.favorite_text_shadow_color)
      }

      // Not favorited yet, set it to a blank
      else {
        favoriteCount.setText("++ ")
        favoriteCount.setBackgroundResource(R.drawable.btn_favorite_default)
        favoriteCount.setShadowLayer(0, 0, 0, Color.TRANSPARENT)
      }

      // Mark this as our favorite
      if (release.isMyFavorite) {
        favoriteCount.setBackgroundResource(R.drawable.btn_favorite_mine)
      }
    }

    // Set the quick contact badge to the author's picture
    val badge = row.findViewById(R.id.module_author_avatar).asInstanceOf[QuickContactBadge]
    if (badge != null) {
      if (gravatar != null && gravatar.getBitmap != null) {
        badge.setImageBitmap(gravatar.getBitmap)
      }
    
      // No user picture, set to default
      else {
        badge.setImageResource(R.drawable.ic_contact_picture)
      }
    }
  }
}
