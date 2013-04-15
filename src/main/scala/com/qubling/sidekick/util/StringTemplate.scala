/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.util

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Map

import android.content.Context
import android.content.res.Resources
import android.util.Log

import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import scala.io.Source

/**
 * A very simple tool for building JSON from a template and set of input
 * variables.
 *
 * @author sterling
 *
 */
object StringTemplate  {
  private val VariablePattern = """%(\w+)\|([js])%""".r("name", "modifier")
}

class StringTemplate(private val context : Context) {

  private def loadTemplate(templateName : String) : String = {
    val assetName = "template/" + templateName + ".tmpl"

//        Log.d("StringTemplate", "Using asset " + assetName + " to build template")

    // Load the JSON query template
    val resources = try {
      context.getResources()
    }
    catch {
      case t : RuntimeException =>
        Log.d("StringTemplate", "bummer", t)
        throw t
    }

//        Log.d("StringTemplate", "getResources()");

    try {
      val moduleSearchTemplateIn = Source.fromInputStream(resources.getAssets().open(assetName))
      moduleSearchTemplateIn.mkString("")
    }
    catch {
      case e : IOException =>
          // TODO Should we do something about this?
          Log.e("StringTemplate", "Error loading " + assetName, e)
          null
    }
  }

  def processTemplate(templateName : String, variables : Map[String, Object]) : String = {
//    	Log.d("StringTemplate", "START processTemplate()");
    	
    val template = loadTemplate(templateName)
    StringTemplate.VariablePattern replaceAllIn (template, (m : Match) => {
      val name   = m group "name"
      val format = m group "modifier"

      val someValue = variables get name
      format match {
        case "j" =>
          someValue match {
            case fragment : JSONFragment => fragment.toJSONString
            case otherValue =>
              try {
                otherValue.toString.toInt.toString
              }
              catch {
                case e : NumberFormatException =>
                  try {
                    otherValue.toString.toDouble.toString
                  }
                  catch {
                    case e : NumberFormatException =>
                      throw new RuntimeException("cannot place [" + otherValue + "] into template [" + templateName + "]");
                  }
              }
          }
        case "s" =>
          "\"".r replaceAllIn (
            if (someValue != null) 
              someValue.toString 
            else 
              "null", (m : Match) => "\\\"")
        case _ =>
          throw new RuntimeException("unknown format specifier [" + format + "] in template [" + templateName + "]");
      }
    })
  }
}
