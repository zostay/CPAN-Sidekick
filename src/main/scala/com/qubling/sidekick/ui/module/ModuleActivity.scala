/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.ui.module

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.Window

import com.qubling.sidekick.instance.Module
import com.qubling.sidekick.instance.Release
import com.qubling.sidekick.search.Search.OnSearchActivity
import com.qubling.sidekick.ui.release.ReleaseViewActivity

/**
 * This is an abstract activity for sharing functionality between the
 * {@link ModuleSearchActivity} and the {@link ModuleViewActivity}.
 *
 * @author sterling
 *
 */
object ModuleActivity {
    val ExtraModule = "com.qubling.sidekick.intent.extra.MODULE";
}

abstract class ModuleActivity extends FragmentActivity with OnSearchActivity {

  override def onCreate(state : Bundle) {
    super.onCreate(state)

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
  }

  override def onSearchStart() {
    runOnUiThread(new Runnable {
      override def run() {
        setProgressBarIndeterminateVisibility(true)
      }
    })
  }

  override def onSearchComplete() {
    runOnUiThread(new Runnable {
      override def run() {
        setProgressBarIndeterminateVisibility(false)
      }
    })
  }

  def startSearch(model : Boolean) {}
  def cancelSearch() {}
  def onModuleClick(clickedModule : Module) {}
    
  def onReleaseClick(clickedModule : Module) {
    val clickedRelease = clickedModule.getRelease()
    val moduleReleaseIntent = new Intent(this, classOf[ReleaseViewActivity])
    moduleReleaseIntent.putExtra(ReleaseViewActivity.EXTRA_RELEASE, clickedRelease)
    moduleReleaseIntent.putExtra(ModuleActivity.ExtraModule, clickedModule)
    startActivity(moduleReleaseIntent)
  }

  def isModuleAcceptableForThisActivity(module : Module) : Boolean = true
}
