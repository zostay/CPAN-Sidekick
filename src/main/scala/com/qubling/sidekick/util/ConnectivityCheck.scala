package com.qubling.sidekick.util

import java.io.IOException

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient

import com.qubling.sidekick.R

import android.app.Activity
import android.widget.Toast

class ConnectivityCheck(val activity : Activity) extends Thread {
  override def run() {
    val testConnectivityClient = new DefaultHttpClient
    val request = new HttpPost("http://api.metacpan.org/")

    try {
      testConnectivityClient.execute(request)
    }
    catch {
      case e : IOException =>
        activity.runOnUiThread(new Runnable {
          override def run() {
            Toast.makeText(activity, R.string.cannot_connect_to_metacpan, Toast.LENGTH_LONG).show()
          }
        })
    }
  }
}
