/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

/**
 * A very simple tool for building JSON from a template and set of input
 * variables.
 *
 * @author sterling
 *
 */
public class StringTemplate {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("%(\\w+)\\|([js])%");

    private Context context;

    public StringTemplate(Context context) {
        this.context = context;
    }

    private String loadTemplate(String templateName) {

        String assetName = "template/" + templateName + ".tmpl";
        
//        Log.d("StringTemplate", "Using asset " + assetName + " to build template");

        // Load the JSON query template
        Resources resources;
        try {
        	resources = context.getResources();
        }
        catch (RuntimeException t) {
        	Log.d("StringTemplate", "bummer", t);
        	throw t;
        }
//        Log.d("StringTemplate", "getResources()");
        try {
            InputStream moduleSearchTemplateIn = resources.getAssets().open(assetName);
//            Log.d("StringTemplate", "open()");
            InputStreamReader moduleSearchTemplateReader = new InputStreamReader(moduleSearchTemplateIn, "UTF-8");
//            Log.d("StringTemplate", "new InputStreamReader()");
            char[] buf = new char[1000];
            StringBuilder moduleSearchTemplateBuilder = new StringBuilder();
            int readLength;
            while ((readLength = moduleSearchTemplateReader.read(buf)) > -1) {
                moduleSearchTemplateBuilder.append(buf, 0, readLength);
            }
            return moduleSearchTemplateBuilder.toString();
        }

        catch (IOException e) {
            // TODO Should we do something about this?
            Log.e("StringTemplate", "Error loading " + assetName, e);
            return null;
        }

    }

    public String processTemplate(String templateName, Map<String, Object> variables) {
//    	Log.d("StringTemplate", "START processTemplate()");
    	
        String template = loadTemplate(templateName);

        StringBuffer completedTemplate = new StringBuffer(template.length());
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String variable = matcher.group(1);
            String format   = matcher.group(2);
            
//            Log.d("StringTemplate", "Found variable "+ variable + " with format " + format);

            Object objectValue = variables.get(variable);
            String value;

            if ("j".equals(format)) {
                if (objectValue instanceof JSONFragment) {
                    value = ((JSONFragment) objectValue).toJSONString();
                }
                else {
                    try {
                        value = String.valueOf(Integer.parseInt(objectValue.toString()));
                    }
                    catch (NumberFormatException e1) {
                        try {
                            value = String.valueOf(Double.parseDouble(objectValue.toString()));
                        }
                        catch (NumberFormatException e2) {
                            throw new RuntimeException("cannot place [" + objectValue + "] into template [" + templateName + "]");
                        }
                    }
                }
            }
            else if ("s".equals(format)) {
                value = objectValue.toString().replaceAll("\"", "\\\"");
            }
            else {
                throw new RuntimeException("unknown format specifier [" + format + "] in template [" + templateName + "]");
            }

//            Log.d("StringTemplate", variable + ": " + value);
            
            matcher.appendReplacement(completedTemplate, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(completedTemplate);
        
//        Log.d("StringTemplate", "END processTemplate()");

        return completedTemplate.toString();
    }
}
