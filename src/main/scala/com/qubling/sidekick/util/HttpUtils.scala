package com.qubling.sidekick.util

import java.io.IOException
import java.io.InputStreamReader

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse

import android.util.Log

import scala.io.Source

object HttpUtils {
  def getCharset(res : HttpResponse) : String = {
    val entity = res.getEntity

    val contentType = entity.getContentType.getValue
    val charsetIndex = contentType.indexOf("charset=")
    if (charsetIndex >= 0) {
      val endingIndex = contentType.indexOf(";", charsetIndex + 8)
      if (endingIndex >= 0) {
        Log.d("HttpUtils", "charset partial")
        contentType.substring(charsetIndex + 8, endingIndex)
      }
      else {
        Log.d("HttpUtils", "charset whole")
        contentType.substring(charsetIndex + 8)
      }
    }
    else {
      Log.d("HttpUtils", "fallback to UTF-8")
      "UTF-8"
    }
  }
  
  def slurpContent(res : HttpResponse) : String = {
    val entity = res.getEntity
    val charset = getCharset(res)

    Log.d("HttpUtils", "charset = " + charset)

    Source.fromInputStream(entity.getContent, charset).mkString("")
  }
}
