package com.qubling.sidekick.metacpan;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class StringTemplate {
	
	private static final Pattern VARIABLE_PATTERN = Pattern.compile("%(\\w+)\\|([js])%");
	
	private Context context;
	
	public StringTemplate(Context context) {
		this.context = context;
	}

	private String loadTemplate(String templateName) {
		
		String assetName = "template/" + templateName + ".tmpl";
		
		// Load the JSON query template
		Resources resources = context.getResources();
		try {
			InputStream moduleSearchTemplateIn = resources.getAssets().open(assetName);
			InputStreamReader moduleSearchTemplateReader = new InputStreamReader(moduleSearchTemplateIn, "UTF-8");
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
			Log.e("MetaCPANSearch", "Error loading " + assetName + ": " + e);
			return null;
		}
		
	}
	
	public String processTemplate(String templateName, Map<String, Object> variables) {
		String template = loadTemplate(templateName);
		
		StringBuffer completedTemplate = new StringBuffer(template.length());
		Matcher matcher = VARIABLE_PATTERN.matcher(template);
		while (matcher.find()) {
			String variable = matcher.group(1);
			String format   = matcher.group(2);
			
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
			
			matcher.appendReplacement(completedTemplate, Matcher.quoteReplacement(value));
		}
		matcher.appendTail(completedTemplate);
		
		return completedTemplate.toString();
	}
}
