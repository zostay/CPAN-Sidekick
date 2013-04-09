/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

import com.bugsense.trace.BugSenseHandler

import com.qubling.sidekick.ui.module.ModuleSearchActivity

/**
 * Some basic utilities.
 */
object Util {
  private val BugSenseAPIKey : Option[String] = None

  def setupBugSense(context : Context) {
    Log.d("Util", "testing")
    BugSenseAPIKey match {
      case Some(key) => BugSenseHandler.setup(context, key)
      case None      => ()
    }
  }
}

/**
 * An activity for starting up the application. This is mostly used to give the
 * application a name of "CPAN", but jump into the module search activity. This
 * may do some other initial application setup or switch activities based upon
 * the type of device, etc.
 *
 * @author sterling
 *
 */
class SidekickLauncher extends Activity {

  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)

    // Setup BugSense
    Util.setupBugSense(this)

    val intent = new Intent(this, classOf[ModuleSearchActivity])
    startActivity(intent)
  }

  override def onPause() {
    super.onPause()

    finish()
  }

}

